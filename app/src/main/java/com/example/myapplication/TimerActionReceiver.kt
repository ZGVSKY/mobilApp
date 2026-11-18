package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Handles the notification action buttons.
 *
 * Pause/Resume only write a [TimerCommand] that MainApp polls and applies
 * (the app is in the background at that moment). Reset additionally stops
 * [TimerService] right away so the notification closes immediately.
 */
class TimerActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PAUSE = "com.example.myapplication.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.myapplication.ACTION_RESUME"
        const val ACTION_RESET = "com.example.myapplication.ACTION_RESET"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PAUSE -> {
                TimerCommand.set(context, TimerCommand.PAUSE)
            }
            ACTION_RESUME -> {
                TimerCommand.set(context, TimerCommand.RESUME)
            }
            ACTION_RESET -> {
                // Write the command first, then close the notification.
                TimerCommand.set(context, TimerCommand.RESET)
                val serviceIntent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_STOP
                }
                // Never crash in the receiver if the service is not running.
                runCatching { context.startService(serviceIntent) }
            }
        }
    }
}
