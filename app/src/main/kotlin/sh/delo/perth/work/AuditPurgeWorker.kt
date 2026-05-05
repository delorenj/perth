package sh.delo.perth.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import sh.delo.perth.core.data.db.dao.CommandAuditDao
import sh.delo.perth.core.domain.repository.SettingsRepository
import timber.log.Timber

/**
 * Periodic worker that prunes the command audit log per the user-configured
 * retention period (Story 8.3).
 *
 * Hilt cannot directly inject into [CoroutineWorker] without `androidx.hilt:hilt-work`,
 * so we resolve dependencies via [EntryPointAccessors] from the SingletonComponent.
 * This avoids the extra dependency while keeping the worker testable in isolation
 * (the pure-time math lives in [retentionCutoffMillis]).
 */
class AuditPurgeWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun commandAuditDao(): CommandAuditDao
        fun settingsRepository(): SettingsRepository
    }

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, Deps::class.java)
        return try {
            val retentionDays = deps.settingsRepository().getAuditRetentionDays()
            val cutoff = retentionCutoffMillis(System.currentTimeMillis(), retentionDays)
            deps.commandAuditDao().purgeOlderThan(cutoff)
            Timber.d("AuditPurgeWorker: purged entries older than %d days (cutoff=%d)", retentionDays, cutoff)
            Result.success()
        } catch (t: Throwable) {
            // Storage failure on a background pass is recoverable; let WorkManager retry
            // with its built-in exponential backoff.
            Timber.w(t, "AuditPurgeWorker failed; will retry")
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "perth.audit.purge"
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

        /**
         * Returns the epoch-millis cutoff for purging. Pure function so the math
         * is unit-testable without Android scaffolding.
         */
        fun retentionCutoffMillis(nowMillis: Long, retentionDays: Int): Long {
            require(retentionDays > 0) { "retentionDays must be > 0; was $retentionDays" }
            return nowMillis - retentionDays * MILLIS_PER_DAY
        }
    }
}
