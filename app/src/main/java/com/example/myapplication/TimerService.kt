package com.example.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground service that shows the silent timer notification while the app
 * is hidden. The notification displays the live time and has two buttons:
 * Pause/Continue (toggles depending on the current state) and Reset
 * (which also closes the notification).
 *
 * The service does not tick by itself — the app sends [ACTION_UPDATE]
 * commands with the current time (throttled by MainApp).
 */
class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UPDATE = "ACTION_UPDATE"
        const val EXTRA_TIME = "EXTRA_TIME"
        const val EXTRA_PAUSED = "EXTRA_PAUSED"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Never restart sticky: without data there is nothing to show, and a
        // restart without startForeground() would crash on Android 8+.
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent.action) {
            ACTION_START -> {
                val time = intent.getStringExtra(EXTRA_TIME) ?: "00:00:000"
                val paused = intent.getBooleanExtra(EXTRA_PAUSED, false)
                startForeground(NOTIFICATION_ID, buildNotification(time, paused))
            }
            ACTION_UPDATE -> {
                val time = intent.getStringExtra(EXTRA_TIME) ?: "00:00:000"
                val paused = intent.getBooleanExtra(EXTRA_PAUSED, false)
                postNotification(time, paused)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_title)
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun actionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(action).setPackage(packageName)
        return PendingIntent.getBroadcast(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(time: String, paused: Boolean): Notification {
        // First button: Pause while running, Continue while paused.
        val toggleAction = if (paused) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                getString(R.string.continue_timer),
                actionPendingIntent(TimerActionReceiver.ACTION_RESUME, 0)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                getString(R.string.pause),
                actionPendingIntent(TimerActionReceiver.ACTION_PAUSE, 0)
            )
        }

        // Second button: Reset — resets the timer and closes the notification.
        val resetAction = NotificationCompat.Action(
            android.R.drawable.ic_delete,
            getString(R.string.reset),
            actionPendingIntent(TimerActionReceiver.ACTION_RESET, 1)
        )

        val contentIntent = packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
            PendingIntent.getActivity(
                this, 0, launch,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(time)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .apply { contentIntent?.let { setContentIntent(it) } }
            .addAction(toggleAction)
            .addAction(resetAction)
            .build()
    }

    private fun postNotification(time: String, paused: Boolean) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(time, paused))
    }
}
