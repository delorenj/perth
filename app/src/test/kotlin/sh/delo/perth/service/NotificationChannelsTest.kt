package sh.delo.perth.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Sanity tests for [NotificationChannels].
 *
 * The actual channel registration logic depends on the Android NotificationManager,
 * which requires Robolectric or instrumented tests. This unit test pins the
 * channel ID so a rename here is caught at build time rather than via a missing
 * notification at runtime.
 */
class NotificationChannelsTest {

    @Test
    fun `session channel id is stable`() {
        // Renaming this constant is a breaking change because Android tracks
        // user-level channel preferences (importance, sound) by ID. Bumping the
        // ID would silently reset those preferences for existing installs.
        assertEquals("perth_session", NotificationChannels.SESSION_CHANNEL_ID)
    }
}
