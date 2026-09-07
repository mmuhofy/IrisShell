package com.iris.irisshell

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import java.io.File

/**
 * DocumentsProvider that exposes Iris Shell's internal Ubuntu rootfs to the
 * Android Storage Access Framework (SAF) — e.g. the "Files" app's "Open by app"
 * picker, or ACTION_OPEN_DOCUMENT.
 *
 * Root: "Iris Shell" → `$context.filesDir/ubuntu/`
 * Children are served recursively using absolute file paths as document IDs.
 *
 * Inspired by: github.com/termux/termux-app (StorageVolumeProvider pattern)
 * Adapted for Iris Shell — com.iris.irisshell
 */
class IrisShellDocumentsProvider : android.provider.DocumentsProvider() {

    private val context: Context
        get() = checkNotNull(getContext()) { "Provider context is null" }

    private val ubuntuDir: File
        get() = File(context.filesDir, "ubuntu").canonicalFile

    companion object {
        const val AUTHORITY = "com.iris.irisshell.documents"
        private const val ROOT_ID = "iris_shell_root"
        private const val DISPLAY_NAME = "Iris Shell"
        private const val MIME_TYPE_DIR = "vnd.android.document/directory"
    }

    override fun queryRoots(projection: Array<String>): Cursor {
        val row = arrayOfNulls<Any?>(projection.size)
        for (i in projection.indices) {
            row[i] = when (projection[i]) {
                DocumentsContract.Root.COLUMN_ROOT_ID -> ROOT_ID
                DocumentsContract.Root.COLUMN_DOCUMENT_ID -> ubuntuDir.absolutePath
                DocumentsContract.Root.COLUMN_TITLE -> DISPLAY_NAME
                DocumentsContract.Root.COLUMN_DESCRIPTION -> "Ubuntu rootfs + PRoot binaries"
                DocumentsContract.Root.COLUMN_MIME_TYPES -> "*/*"
                DocumentsContract.Root.COLUMN_FLAGS -> (
                    DocumentsContract.Root.FLAG_SUPPORTS_CREATE or
                    DocumentsContract.Root.FLAG_SUPPORTS_RECENT_DELETE or
                    DocumentsContract.Root.FLAG_SUPPORTS_SEARCH
                )
                DocumentsContract.Root.COLUMN_AVAILABLE_BYTES -> null
                else -> null
            }
        }
        return MatrixCursor(projection).apply { addRow(row) }
    }

    override fun queryDocument(documentId: String, projection: Array<String>): Cursor {
        val file = documentIdToFile(documentId)
        return singleFileCursor(file, documentId, projection)
    }

    override fun queryChildDocuments(
        parentDocumentId: String,
        projection: Array<String>,
        sortOrder: String?,
    ): Cursor {
        val parent = documentIdToFile(parentDocumentId)
        val cursor = MatrixCursor(projection)
        if (!parent.isDirectory) return cursor

        val children = parent.listFiles() ?: return cursor
        for (child in children) {
            val childId = child.absolutePath
            val row = arrayOfNulls<Any?>(projection.size)
            for (i in projection.indices) {
                row[i] = getColumnValue(child, childId, projection[i])
            }
            cursor.addRow(row)
        }
        return cursor
    }

    override fun openDocument(
        documentId: String,
        mode: String,
        signal: CancellationSignal?,
    ): ParcelFileDescriptor {
        val file = documentIdToFile(documentId)
        val pfdMode = if (mode.contains("w") || mode.contains("a")) {
            ParcelFileDescriptor.MODE_READ_WRITE
        } else {
            ParcelFileDescriptor.MODE_READ_ONLY
        }
        return ParcelFileDescriptor.open(file, pfdMode)
    }

    override fun createDocument(parentDocumentId: String, mimeType: String, displayName: String): String {
        val parent = documentIdToFile(parentDocumentId)
        val child = File(parent, displayName)
        if (child.exists()) {
            throw IllegalArgumentException("Already exists: $displayName")
        }
        if (mimeType == MIME_TYPE_DIR) {
            child.mkdirs()
        } else {
            child.parentFile?.mkdirs()
            child.createNewFile()
        }
        return child.absolutePath
    }

    override fun deleteDocument(documentId: String) {
        val file = documentIdToFile(documentId)
        if (!file.delete()) throw SecurityException("Cannot delete: ${file.absolutePath}")
    }

    override fun renameDocument(documentId: String, displayName: String): String {
        val file = documentIdToFile(documentId)
        val newFile = File(file.parentFile, displayName)
        if (!file.renameTo(newFile)) throw SecurityException("Rename failed")
        return newFile.absolutePath
    }

    override fun onCreate(): Boolean = ubuntuDir.exists()

    // ─── Helpers ─────────────────────────────────────

    private fun documentIdToFile(documentId: String): File {
        val f = File(documentId).canonicalFile
        if (!f.exists()) throw IllegalArgumentException("Document not found: $documentId")
        if (!f.toPath().startsWith(ubuntuDir.toPath())) {
            throw IllegalArgumentException("Path outside ubuntu rootfs: $documentId")
        }
        return f
    }

    private fun singleFileCursor(file: File, docId: String, projection: Array<String>): Cursor {
        val row = arrayOfNulls<Any?>(projection.size)
        for (i in projection.indices) {
            row[i] = getColumnValue(file, docId, projection[i])
        }
        return MatrixCursor(projection).apply { addRow(row) }
    }

    private fun getColumnValue(file: File, docId: String, columnName: String): Any? = when (columnName) {
        DocumentsContract.Document.COLUMN_DOCUMENT_ID -> docId
        DocumentsContract.Document.COLUMN_DISPLAY_NAME -> file.name
        DocumentsContract.Document.COLUMN_MIME_TYPE ->
            if (file.isDirectory) MIME_TYPE_DIR else inferMimeType(file)
        DocumentsContract.Document.COLUMN_SIZE -> if (file.isFile) file.length() else 0L
        DocumentsContract.Document.COLUMN_LAST_MODIFIED -> file.lastModified().toLong()
        DocumentsContract.Document.COLUMN_FLAGS -> inferFlags(file)
        else -> null
    }

    private fun inferMimeType(file: File): String {
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
        return if (ext.isNullOrEmpty()) {
            "application/octet-stream"
        } else {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                ?: "application/octet-stream"
        }
    }

    private fun inferFlags(file: File): Int = (
        DocumentsContract.Document.FLAG_SUPPORTS_OPEN or
        DocumentsContract.Document.FLAG_SUPPORTS_TRANSFER_UNCONDITIONALLY or
        DocumentsContract.Document.FLAG_NO_TYPE_TRANSFORMATION
    )
}
