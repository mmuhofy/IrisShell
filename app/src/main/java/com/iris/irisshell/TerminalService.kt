package com.iris.irisshell

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.iris.irisshell.domain.session.SessionRepository
import com.iris.irisshell.terminal.TerminalManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that keeps PTY sessions alive beyond the Activity lifecycle.
 *
 * Inspired by Termux's `TermuxService` (github.com/termux/termux-app,
 * TermuxService.kt) — `startForeground` with a notification showing session
 * count, `START_NOT_STICKY` so a system kill doesn't recreate us, and an
 * exit action button in the notification that tears everything down.
 *
 * The service injects the same Hilt singleton [TerminalManager] that
 * [MainActivity] uses, so PTY sessions survive process-level death by
 * the Activity — the system is far less likely to kill a foreground process.
 */
@AndroidEntryPoint
class TerminalService : LifecycleService() {

    @Inject
    lateinit var terminalManager: TerminalManager

    @Inject
    lateinit var sessionRepository: SessionRepository

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: TerminalService get() = this@TerminalService
    }

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
        observeSessionCount()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                terminalManager.destroy()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            null -> {
                val notification = buildNotification(terminalManager.tabCount)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID, notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        terminalManager.destroy()
        super.onDestroy()
    }

    /**
     * Reactive collection of [TerminalManager.sessionCountFlow].
     *
     * - Updates the foreground notification with the live session count.
     * - When the count drops to 0 and [SessionRepository.shouldExit] is true,
     *   calls [stopSelf] — mirroring Termux's `requestStopService` pattern
     *   triggered from `updateNotification` when `mTermuxSessions.isEmpty()`.
     */
    private fun observeSessionCount() {
        lifecycleScope.launch {
            terminalManager.sessionCountFlow.collectLatest { count ->
                if (count == 0) {
                    if (sessionRepository.shouldExit.value) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                        return@collectLatest
                    }
                }
                val notification = buildNotification(count)
                val nm = getSystemService<NotificationManager>()
                nm?.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun buildNotification(sessionCount: Int): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val exitIntent = Intent(this, TerminalService::class.java).apply {
            action = ACTION_STOP
        }
        val exitPending = PendingIntent.getService(
            this, 0, exitIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val sessionText = if (sessionCount == 1) {
            getString(R.string.notification_one_session)
        } else {
            getString(R.string.notification_session_count, sessionCount)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.terminal_service_name))
            .setContentText(sessionText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_notification, getString(R.string.notification_action_exit), exitPending)
            .setOngoing(true)
            .build()
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.terminal_service_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
            val nm = getSystemService<NotificationManager>()
            nm?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1337
        const val CHANNEL_ID = "iris_terminal_service"
        const val ACTION_STOP = "com.iris.irisshell.action.STOP_SERVICE"
    }
}
