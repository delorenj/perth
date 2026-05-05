package sh.delo.perth

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import sh.delo.perth.work.AuditWorkScheduler
import timber.log.Timber

@HiltAndroidApp
class PerthApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initLogging()
        // Story 8.3: schedule the daily audit-log retention pass. KEEP policy makes
        // this safe to call on every cold start.
        AuditWorkScheduler.schedule(this)
    }

    private fun initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        Timber.d("PerthApp initialized")
    }
}

/** Production logging tree that suppresses debug logs and avoids leaking sensitive data. */
private class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == android.util.Log.VERBOSE || priority == android.util.Log.DEBUG) return
        android.util.Log.println(priority, tag ?: "Perth", message)
        t?.let { Timber.e(it) }
    }
}
