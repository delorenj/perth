package sh.delo.perth.work

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Pure unit tests for [AuditPurgeWorker.retentionCutoffMillis].
 *
 * The Android Worker runtime is exercised by the WorkManager test harness
 * (instrumented), but the date arithmetic is the only place a bug would
 * silently corrupt the audit log, so we pin it here.
 */
class AuditPurgeWorkerTest {

    @ParameterizedTest(name = "now={0}, days={1} -> cutoff={2}")
    @CsvSource(
        // Round numbers
        "1000000000000,  1,  999913600000",     // -86_400_000 ms
        "1000000000000,  7,  999395200000",     // -7 * 86_400_000 ms
        "1000000000000, 90,  992224000000",     // default retention
        "1000000000000, 365, 968464000000",     // max retention
        // Zero-time origin
        "0,             1,  -86400000",
    )
    fun cutoffMath(now: Long, days: Int, expected: Long) {
        assertEquals(expected, AuditPurgeWorker.retentionCutoffMillis(now, days))
    }

    @Test
    fun `negative retention is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AuditPurgeWorker.retentionCutoffMillis(System.currentTimeMillis(), -1)
        }
    }

    @Test
    fun `zero retention is rejected`() {
        // Allowing 0 would purge everything, including this morning's commands —
        // protect against accidental zeroing via a future config UI bug.
        assertThrows(IllegalArgumentException::class.java) {
            AuditPurgeWorker.retentionCutoffMillis(System.currentTimeMillis(), 0)
        }
    }

    @Test
    fun `unique work name is stable`() {
        // Renaming this constant would orphan existing scheduled work on upgrade.
        // WorkManager keys schedules by name, not class.
        assertEquals("perth.audit.purge", AuditPurgeWorker.UNIQUE_NAME)
    }

    @Test
    fun `MILLIS_PER_DAY constant is correct`() {
        assertEquals(86_400_000L, AuditPurgeWorker.MILLIS_PER_DAY)
    }
}
