package com.iris.irisshell.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

object IrisIcons {

    private fun iv(
        name: String,
        pathData: List<String>,
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        val parser = PathParser()
        pathData.forEach { data ->
            addPath(
                pathData = parser.parsePathString(data).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

    val ALargeSmall: ImageVector = iv(
        "a_large_small",
        listOf(
            "m15,16l2.536,-7.328a1.02,1.02 1 0,1 1.928,0L22,16",
            "M15.697,14h5.606",
            "m2,16l4.039,-9.69a0.5,0.5 0 0,1 0.923,0L11,16",
            "M3.304,13h6.392"
        )
    )

    val ArrowBigDown: ImageVector = iv(
        "arrow_big_down",
        listOf("M9,5a1,1 0 0,1 1,-1h4a1,1 0 0,1 1,1v6a1,1 0 0,0 1,1h3.293a0.707,0.707 0 0,1 0.5,1.207l-7.086,7.086a1,1 0 0,1 -1.414,0l-7.086,-7.086a0.707,0.707 0 0,1 0.5,-1.207H8a1,1 0 0,0 1,-1Z")
    )

    val ArrowBigLeft: ImageVector = iv(
        "arrow_big_left",
        listOf("M10.793,19.793a0.707,0.707,0,0,0,1.207,-0.5V16a1,1,0,0,1,1,-1h6a1,1,0,0,0,1,-1v-4a1,1,0,0,0,-1,-1h-6a1,1,0,0,1,-1,-1V4.707a0.707,0.707,0,0,0,-1.207,-0.5l-6.94,6.94a1.207,1.207,0,0,0,0,1.707Z")
    )

    val ArrowBigRight: ImageVector = iv(
        "arrow_big_right",
        listOf("M13.207,19.793a0.707,0.707 0 0,1 -1.207,-0.5V16a1,1 0 0,0 -1,-1H5a1,1 0 0,1 -1,-1v-4a1,1 0 0,1 1,-1h6a1,1 0 0,0 1,-1V4.707a0.707,0.707 0 0,1 1.207,-0.5l6.94,6.94a1.207,1.207 0 0,1 0,1.707Z")
    )

    val ArrowBigUp: ImageVector = iv(
        "arrow_big_up",
        listOf("M9,19a1,1 0 0,0 1,1h4a1,1 0 0,0 1,-1v-6a1,1 0 0,1 1,-1h3.293a0.707,0.707 0 0,0 0.5,-1.207l-7.086,-7.086a1,1 0 0,0 -1.414,0l-7.086,7.086a0.707,0.707 0 0,0 0.5,1.207H8a1,1 0 0,1 1,1Z")
    )

    val ArrowDown: ImageVector = iv(
        "arrow_down",
        listOf(
            "M12,5v14",
            "m19,12l-7,7l-7,-7"
        )
    )

    val ArrowLeft: ImageVector = iv(
        "arrow_left",
        listOf(
            "m12,19l-7,-7l7,-7",
            "M19,12H5"
        )
    )

    val ArrowUp: ImageVector = iv(
        "arrow_up",
        listOf(
            "m5,12l7,-7l7,7",
            "M12,19V5"
        )
    )

    val Check: ImageVector = iv(
        "check",
        listOf("M20,6L9,17l-5,-5")
    )

    val ChevronDown: ImageVector = iv(
        "chevron_down",
        listOf("m6,9l6,6l6,-6")
    )

    val ChevronUp: ImageVector = iv(
        "chevron_up",
        listOf("m18,15l-6,-6l-6,6")
    )

    val Copy: ImageVector = iv(
        "copy",
        listOf(
            "M10,8H20A2,2 0 0,1 22,10V20A2,2 0 0,1 20,22H10A2,2 0 0,1 8,20V10A2,2 0 0,1 10,8Z",
            "M4,16c-1.1,0 -2,-0.9 -2,-2V4c0,-1.1 0.9,-2 2,-2h10c1.1,0 2,0.9 2,2"
        )
    )

    val Download: ImageVector = iv(
        "download",
        listOf(
            "M12,15V3",
            "M21,15v4a2,2 0 0,1 -2,2H5a2,2 0 0,1 -2,-2v-4",
            "m7,10l5,5l5,-5"
        )
    )

    val EllipsisVertical: ImageVector = iv(
        "ellipsis_vertical",
        listOf(
            "M13,12A1,1 0 0,1 12,13A1,1 0 0,1 11,12A1,1 0 0,1 13,12Z",
            "M13,5A1,1 0 0,1 12,6A1,1 0 0,1 11,5A1,1 0 0,1 13,5Z",
            "M13,19A1,1 0 0,1 12,20A1,1 0 0,1 11,19A1,1 0 0,1 13,19Z"
        )
    )

    val Keyboard: ImageVector = iv(
        "keyboard",
        listOf(
            "M10,8h0.01",
            "M12,12h0.01",
            "M14,8h0.01",
            "M16,12h0.01",
            "M18,8h0.01",
            "M6,8h0.01",
            "M7,16h10",
            "M8,12h0.01",
            "M4,4H20A2,2 0 0,1 22,6V18A2,2 0 0,1 20,20H4A2,2 0 0,1 2,18V6A2,2 0 0,1 4,4Z"
        )
    )

    val KeyboardOff: ImageVector = iv(
        "keyboard_off",
        listOf(
            "M20,4A2,2 0 0,1 22,6",
            "M22,6L22,16.41",
            "M7,16L16,16",
            "M9.69,4L20,4",
            "M14,8h0.01",
            "M18,8h0.01",
            "m2,2l20,20",
            "M20,20H4a2,2 0 0,1 -2,-2V6a2,2 0 0,1 2,-2",
            "M6,8h0.01",
            "M8,12h0.01"
        )
    )

    val Lock: ImageVector = iv(
        "lock",
        listOf(
            "M5,11H19A2,2 0 0,1 21,13V20A2,2 0 0,1 19,22H5A2,2 0 0,1 3,20V13A2,2 0 0,1 5,11Z",
            "M7,11V7a5,5 0 0,1 10,0v4"
        )
    )

    val Maximize: ImageVector = iv(
        "maximize",
        listOf(
            "M8,3H5a2,2 0 0,0 -2,2v3",
            "M21,8V5a2,2 0 0,0 -2,-2h-3",
            "M3,16v3a2,2 0 0,0 2,2h3",
            "M16,21h3a2,2 0 0,0 2,-2v-3"
        )
    )

    val Minimize: ImageVector = iv(
        "minimize",
        listOf(
            "M8,3v3a2,2 0 0,1 -2,2H3",
            "M21,8h-3a2,2 0 0,1 -2,-2V3",
            "M3,16h3a2,2 0 0,1 2,2v3",
            "M16,21v-3a2,2 0 0,1 2,-2h3"
        )
    )

    val PanelLeft: ImageVector = iv(
        "panel_left",
        listOf(
            "M5,3H19A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3Z",
            "M9,3v18"
        )
    )

    val Pencil: ImageVector = iv(
        "pencil",
        listOf(
            "M21.174,6.812a1,1 0 0,0 -3.986,-3.987L3.842,16.174a2,2 0 0,0 -0.5,0.83l-1.321,4.352a0.5,0.5 0 0,0 0.623,0.622l4.353,-1.32a2,2 0 0,0 0.83,-0.497Z",
            "m15,5l4,4"
        )
    )

    val Play: ImageVector = iv(
        "play",
        listOf("M5,5a2,2 0 0,1 3.008,-1.728l11.997,6.998a2,2 0 0,1 0.003,3.458l-12,7A2,2 0 0,1 5,19Z")
    )

    val Plus: ImageVector = iv(
        "plus",
        listOf(
            "M5,12h14",
            "M12,5v14"
        )
    )

    val RotateCw: ImageVector = iv(
        "rotate_cw",
        listOf(
            "M21,12a9,9 0 1,1 -9,-9c2.52,0 4.93,1 6.74,2.74L21,8",
            "M21,3v5h-5"
        )
    )

    val Search: ImageVector = iv(
        "search",
        listOf(
            "m21,21l-4.34,-4.34",
            "M19,11A8,8 0 0,1 11,19A8,8 0 0,1 3,11A8,8 0 0,1 19,11Z"
        )
    )

    val Settings: ImageVector = iv(
        "settings",
        listOf(
            "M9.671,4.136a2.34,2.34 0 0,1 4.659,0a2.34,2.34 0 0,0 3.319,1.915a2.34,2.34 0 0,1 2.33,4.033a2.34,2.34 0 0,0 0,3.831a2.34,2.34 0 0,1 -2.33,4.033a2.34,2.34 0 0,0 -3.319,1.915a2.34,2.34 0 0,1 -4.659,0a2.34,2.34 0 0,0 -3.32,-1.915a2.34,2.34 0 0,1 -2.33,-4.033a2.34,2.34 0 0,0 0,-3.831A2.34,2.34 0 0,1 6.35,6.051a2.34,2.34 0 0,0 3.319,-1.915",
            "M15,12A3,3 0 0,1 12,15A3,3 0 0,1 9,12A3,3 0 0,1 15,12Z"
        )
    )

    val Square: ImageVector = iv(
        "square",
        listOf("M5,3H19A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3Z")
    )

    val SquarePlus: ImageVector = iv(
        "square_plus",
        listOf(
            "M5,3H19A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3Z",
            "M8,12h8",
            "M12,8v8"
        )
    )

    val SquareTerminal: ImageVector = iv(
        "square_terminal",
        listOf(
            "m7,11l2,-2l-2,-2",
            "M11,13h4",
            "M5,3H19A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3Z"
        )
    )

    val Terminal: ImageVector = iv(
        "terminal",
        listOf(
            "M12,19h8",
            "m4,17l6,-6l-6,-6"
        )
    )

    val Trash2: ImageVector = iv(
        "trash_2",
        listOf(
            "M10,11v6",
            "M14,11v6",
            "M19,6v14a2,2 0 0,1 -2,2H7a2,2 0 0,1 -2,-2V6",
            "M3,6h18",
            "M8,6V4a2,2 0 0,1 2,-2h4a2,2 0 0,1 2,2v2"
        )
    )

    val Undo: ImageVector = iv(
        "undo",
        listOf(
            "M3,7v6h6",
            "M21,17a9,9 0 0,0 -9,-9a9,9 0 0,0 -6,2.3L3,13"
        )
    )

    val X: ImageVector = iv(
        "x",
        listOf(
            "M18,6L6,18",
            "m6,6l12,12"
        )
    )

    val XCircle: ImageVector = iv(
        "x_circle",
        listOf(
            "M22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 22,12Z",
            "m15,9l-6,6",
            "m9,9l6,6"
        )
    )

}