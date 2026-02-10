package com.alpwarestudio.darkswitch.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.alpwarestudio.darkswitch.R

/**
 * Helper responsible for creating and managing the foreground service notification.
 *
 * This notification keeps {@link DarkApplyService} alive while force-dark logic
 * is actively running in the background.
 */
object ServiceNotification {
    // Stable notification channel ID for the foreground apply service.
    private const val CHANNEL_ID = "darkswitch_apply"

    /**
     * Ensures that the notification channel required for the foreground service exists.
     *
     * Safe to call multiple times.
     */
    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Low importance prevents sound/vibration while keeping the service in the foreground.
        val ch = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        nm.createNotificationChannel(ch)
    }

    /**
     * Builds the persistent foreground service notification.
     */
    fun build(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text_force_dark_running))
            // Mark as ongoing so the system treats this as an active foreground service.
            .setOngoing(true)
            .build()
    }
}