package sh.delo.perth.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Schedules the [AuditPurgeWorker] so the command audit log is pruned to the
 * user-configured retention window in the background.
 *
 * Constraints chosen for the runtime profile:
 *  - `setRequiresBatteryNotLow(true)` — pruning is non-urgent; defer when low.
 *  - 24-hour interval — once-daily is more than enough granularity for a 90-day
 *    default retention. WorkManager's minimum interval is 15 minutes, so this is
 *    well within bounds.
 *  - flex window of 6 hours — lets Android batch with other periodic work.
 *  - Exponential backoff on retry, starting at 30 minutes.
 *
 * `KEEP` policy: re-scheduling on every app launch is idempotent. If the user
 * changes the retention preference we cancel and re-schedule explicitly via
 * [reschedule].
 */
object AuditWorkScheduler {

    private val PERIODIC_INTERVAL_HOURS = 24L
    private val FLEX_INTERVAL_HOURS = 6L
    private val BACKOFF_DELAY_MIN = 30L

    fun schedule(context: Context) {
        val request = buildRequest()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AuditPurgeWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
        Timber.d("AuditWorkScheduler: scheduled %s every %dh", AuditPurgeWorker.UNIQUE_NAME, PERIODIC_INTERVAL_HOURS)
    }

    /** Force-replace the existing schedule. Call after the user changes retention. */
    fun reschedule(context: Context) {
        val request = buildRequest()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AuditPurgeWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
        Timber.d("AuditWorkScheduler: rescheduled %s", AuditPurgeWorker.UNIQUE_NAME)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(AuditPurgeWorker.UNIQUE_NAME)
    }

    private fun buildRequest() =
        PeriodicWorkRequestBuilder<AuditPurgeWorker>(
            repeatInterval = PERIODIC_INTERVAL_HOURS, repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = FLEX_INTERVAL_HOURS, flexTimeIntervalUnit = TimeUnit.HOURS,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_MIN, TimeUnit.MINUTES)
            .build()
}
