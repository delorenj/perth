package sh.delo.perth.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.app.Service
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import sh.delo.perth.MainActivity
import sh.delo.perth.R
import timber.log.Timber

/**
 * Foreground service that anchors the lifetime of an active Perth session.
 *
 * Responsibilities:
 *  - Promote the app to the foreground while a Zellij session is active or the
 *    microphone is in use, so that long-lived WebSocket and audio capture work
 *    survive process death prioritization on modern Android.
 *  - Display a persistent low-importance notification with a tap action that
 *    reopens [MainActivity].
 *  - Declare `microphone|dataSync` foreground service types so the runtime grants
 *    the matching capability access (Android 14+ enforces the type↔permission match).
 *
 * Lifecycle ownership: the singleton `ZellijTransport` continues to live in the
 * Hilt SingletonComponent. This service does not own the transport directly; it
 * exists to keep the process alive and visible while the transport is in use.
 *
 * Start: [start] from anywhere with a Context (typically MainActivity.onCreate).
 * Stop:  [stop] when the user signs out or closes the app.
 */
@AndroidEntryPoint
class PerthForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureChannels(this)
        promoteToForeground()
        Timber.d("PerthForegroundService started")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // STICKY: if the system kills us under memory pressure, recreate when possible
        // so the user's session and notification reappear automatically.
        return START_STICKY
    }

    override fun onDestroy() {
        Timber.d("PerthForegroundService stopping")
        super.onDestroy()
    }

    private fun promoteToForeground() {
        val notification = buildNotification()
        // ServiceCompat handles the API-level differences between
        // startForeground(int, Notification) and the typed variant introduced in Q.
        ServiceCompat.startForeground(
            /* service = */ this,
            /* id = */ NOTIFICATION_ID,
            /* notification = */ notification,
            /* foregroundServiceType = */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            },
        )
    }

    private fun buildNotification(): Notification {
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, NotificationChannels.SESSION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_session_title))
            .setContentText(getString(R.string.notification_session_text))
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val REQUEST_OPEN_APP = 100

        /** Starts the foreground service. Idempotent — safe to call repeatedly. */
        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, PerthForegroundService::class.java),
            )
        }

        /** Stops the foreground service if running. */
        fun stop(context: Context) {
            context.stopService(Intent(context, PerthForegroundService::class.java))
        }
    }
}
