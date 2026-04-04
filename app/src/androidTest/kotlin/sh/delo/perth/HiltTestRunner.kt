package sh.delo.perth

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom [AndroidJUnitRunner] that replaces the application class with
 * [HiltTestApplication] so Hilt can inject dependencies in instrumented tests.
 *
 * Configured as the test instrumentation runner in app/build.gradle.kts:
 * `testInstrumentationRunner = "sh.delo.perth.HiltTestRunner"`
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
