package sh.delo.perth.core.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import sh.delo.perth.core.result.AppException

/**
 * Coverage for [ErrorMapper]. The mapper is the single boundary where exceptions
 * become user-facing strings; bugs here either leak stack traces to the UI or
 * leave the user stuck with no recovery path. Both are Story 6.1 violations,
 * so every [AppException] subtype is exercised here.
 */
class ErrorMapperTest {

    // region Universal invariants — apply to every AppException subtype

    @ParameterizedTest(name = "non-empty message: {0}")
    @MethodSource("allExceptions")
    fun `every exception produces a non-empty message`(exception: AppException) {
        val presentation = ErrorMapper.map(exception)
        assertTrue(
            presentation.message.isNotBlank(),
            "Expected non-blank message for ${exception::class.simpleName}",
        )
    }

    @ParameterizedTest(name = "non-empty actions: {0}")
    @MethodSource("allExceptions")
    fun `every exception produces at least one recovery action`(exception: AppException) {
        val presentation = ErrorMapper.map(exception)
        assertTrue(
            presentation.recoveryActions.isNotEmpty(),
            "Expected at least one RecoveryAction for ${exception::class.simpleName}",
        )
    }

    @ParameterizedTest(name = "no leaked technical jargon: {0}")
    @MethodSource("allExceptions")
    fun `messages do not leak stack traces or fully-qualified class names`(exception: AppException) {
        val message = ErrorMapper.map(exception).message
        // Stack traces and Kotlin/Java internals should never reach the UI.
        assertFalse(message.contains("kotlin."), "Message leaked Kotlin internals: $message")
        assertFalse(message.contains("java."), "Message leaked Java internals: $message")
        assertFalse(message.contains("at sh.delo"), "Message leaked stack frame: $message")
        assertFalse(message.contains("Exception"), "Message leaked exception class name: $message")
    }

    @ParameterizedTest(name = "Retry available: {0}")
    @MethodSource("allExceptions")
    fun `Retry button is always available per Story 6_1`(exception: AppException) {
        // Story 6.1 AC: "Retry button always available". For Authentication this is
        // a secondary action (after CheckApiKey) but it must still be present so the
        // user is never stuck with no path forward.
        val actions = ErrorMapper.map(exception).recoveryActions
        assertTrue(
            actions.contains(RecoveryAction.Retry),
            "Expected RecoveryAction.Retry in actions for ${exception::class.simpleName}; got $actions",
        )
    }

    // endregion

    // region Per-subtype message and recovery semantics

    @Nested
    @DisplayName("Network errors")
    inner class NetworkMapping {
        @Test
        fun `message identifies the network category and surfaces a hint`() {
            val out = ErrorMapper.map(AppException.Network("DNS unresolved"))
            assertTrue(out.message.contains("Network", ignoreCase = true))
            assertTrue(out.message.contains("DNS unresolved"))
        }

        @Test
        fun `recovery offers Retry and Reconnect`() {
            val actions = ErrorMapper.map(AppException.Network("x")).recoveryActions
            assertTrue(actions.contains(RecoveryAction.Retry))
            assertTrue(actions.contains(RecoveryAction.Reconnect))
        }

        @Test
        fun `blank cause does not produce dangling parentheses in the message`() {
            val out = ErrorMapper.map(AppException.Network("   "))
            assertFalse(out.message.contains("()"), "Got: ${out.message}")
        }
    }

    @Nested
    @DisplayName("Server errors")
    inner class ServerMapping {
        @Test
        fun `5xx surfaces 'unexpected response' phrasing`() {
            val out = ErrorMapper.map(AppException.Server(503, "downstream failed"))
            assertTrue(out.message.contains("503"))
            assertTrue(out.message.contains("unexpected", ignoreCase = true))
        }

        @Test
        fun `4xx surfaces 'rejected' phrasing`() {
            val out = ErrorMapper.map(AppException.Server(404, "missing route"))
            assertTrue(out.message.contains("404"))
            assertTrue(out.message.contains("rejected", ignoreCase = true))
        }

        @Test
        fun `non-HTTP code falls into the generic branch`() {
            val out = ErrorMapper.map(AppException.Server(0, "weird"))
            assertTrue(out.message.contains("0"))
        }
    }

    @Nested
    @DisplayName("Voice errors")
    inner class VoiceMapping {
        @Test
        fun `mic permission keyword routes to settings flow`() {
            val out = ErrorMapper.map(AppException.Voice("RECORD_AUDIO permission missing"))
            assertTrue(out.message.contains("Microphone", ignoreCase = true))
            assertTrue(out.recoveryActions.contains(RecoveryAction.OpenSettings))
            assertTrue(out.recoveryActions.contains(RecoveryAction.TypeInstead))
        }

        @Test
        fun `transcription failure routes to retry+type-instead`() {
            val out = ErrorMapper.map(AppException.Voice("recognizer returned empty result"))
            assertTrue(out.message.contains("transcribe", ignoreCase = true))
            assertTrue(out.recoveryActions.contains(RecoveryAction.Retry))
            assertTrue(out.recoveryActions.contains(RecoveryAction.TypeInstead))
        }

        @Test
        fun `microphone keyword variant also triggers permission flow`() {
            val out = ErrorMapper.map(AppException.Voice("Microphone unavailable"))
            assertTrue(out.recoveryActions.contains(RecoveryAction.OpenSettings))
        }
    }

    @Nested
    @DisplayName("Command errors")
    inner class CommandMapping {
        @Test
        fun `LLM keyword routes to API key recovery`() {
            val out = ErrorMapper.map(AppException.Command("LLM returned malformed JSON"))
            assertTrue(out.message.contains("interpretation", ignoreCase = true))
            assertTrue(out.recoveryActions.contains(RecoveryAction.CheckApiKey))
        }

        @Test
        fun `execution failure surfaces the underlying detail`() {
            val out = ErrorMapper.map(AppException.Command("rm: permission denied"))
            assertTrue(out.message.contains("rm: permission denied"))
            assertTrue(out.message.contains("No further commands"))
        }

        @Test
        fun `api key keyword variant also triggers LLM branch`() {
            val out = ErrorMapper.map(AppException.Command("invalid API key for OpenAI"))
            assertTrue(out.recoveryActions.contains(RecoveryAction.CheckApiKey))
        }
    }

    @Nested
    @DisplayName("Authentication errors")
    inner class AuthMapping {
        @Test
        fun `auth message points the user to Settings`() {
            val out = ErrorMapper.map(AppException.Authentication("401 Unauthorized"))
            assertTrue(out.message.contains("Settings"))
        }

        @Test
        fun `auth recovery prefers CheckApiKey first, Retry second`() {
            // Order matters: CheckApiKey is the useful action. Retry exists only to
            // satisfy the "always available" guarantee.
            val actions = ErrorMapper.map(AppException.Authentication("bad creds")).recoveryActions
            assertEquals(RecoveryAction.CheckApiKey, actions.first())
            assertTrue(actions.contains(RecoveryAction.Retry))
        }
    }

    @Nested
    @DisplayName("Timeout errors")
    inner class TimeoutMapping {
        @Test
        fun `timeout message asks the user to retry`() {
            val out = ErrorMapper.map(AppException.Timeout("read timed out"))
            assertTrue(out.message.contains("timed out", ignoreCase = true))
            assertTrue(out.recoveryActions.contains(RecoveryAction.Retry))
        }
    }

    @Nested
    @DisplayName("Storage errors")
    inner class StorageMapping {
        @Test
        fun `storage error message warns about lost data and offers retry`() {
            val out = ErrorMapper.map(AppException.Storage("disk full"))
            assertTrue(out.message.contains("storage", ignoreCase = true))
            assertTrue(out.recoveryActions.contains(RecoveryAction.Retry))
            assertTrue(out.recoveryActions.contains(RecoveryAction.Dismiss))
        }
    }

    // endregion

    // region Smoke test on the data class itself

    @Test
    fun `ErrorPresentation is a data class with usable equals`() {
        val a = ErrorMapper.ErrorPresentation("x", listOf(RecoveryAction.Retry))
        val b = ErrorMapper.ErrorPresentation("x", listOf(RecoveryAction.Retry))
        assertEquals(a, b)
    }

    // endregion

    companion object {
        @JvmStatic
        fun allExceptions(): List<AppException> = listOf(
            AppException.Network("connection refused"),
            AppException.Timeout("read timed out"),
            AppException.Server(500, "internal error"),
            AppException.Server(403, "forbidden"),
            AppException.Authentication("401"),
            AppException.Voice("permission missing"),
            AppException.Voice("recognizer returned empty"),
            AppException.Command("LLM error"),
            AppException.Command("execution failed: rm denied"),
            AppException.Storage("disk full"),
        ).also { samples ->
            // Sanity check: every AppException subtype is represented in the sample set
            // so the parameterized invariants exercise the full sealed hierarchy.
            val expectedSubtypes = setOf(
                "Network", "Timeout", "Server", "Authentication", "Voice", "Command", "Storage",
            )
            val actualSubtypes = samples.map { it::class.simpleName }.toSet()
            check(actualSubtypes == expectedSubtypes) {
                "allExceptions sample is missing subtypes. Expected: $expectedSubtypes, got: $actualSubtypes"
            }
        }
    }
}
