package sh.delo.perth.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

/**
 * Notification channel registry for Perth.
 *
 * Channels must be created before the first notification posts to them; calling
 * [ensureChannels] is idempotent and safe to invoke from [PerthForegroundService.onCreate].
 *
 * Channel IDs are intentionally stable strings rather than enum names so that
 * production traffic survives package renames or refactors.
 */
internal object NotificationChannels {

    /** Persistent low-importance channel for the foreground-service notification. */
    const val SESSION_CHANNEL_ID = "perth_session"

    /**
     * Creates the [SESSION_CHANNEL_ID] channel if needed.
     *
     * On API < 26 (Android 7.x and below) channels do not exist; this is a no-op.
     * minSdk for Perth is 28, so the API check is defensive but not strictly required.
     */
    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(SESSION_CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            SESSION_CHANNEL_ID,
            "Active session",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shown while Perth is connected to a Zellij session or capturing voice"
            setShowBadge(false)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }
}
