package sh.delo.perth

import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.PaneOutput
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.model.Transcript
import sh.delo.perth.core.domain.model.ZellijPane
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.domain.model.ZellijTab
import java.time.Instant

/**
 * Shared test data factories for use across unit tests and instrumented tests.
 */
object TestData {

    // ---------------------------------------------------------------------------
    // PaneIds
    // ---------------------------------------------------------------------------

    val PANE_ID_1 = PaneId("test-pane-1")
    val PANE_ID_2 = PaneId("test-pane-2")
    val PANE_ID_3 = PaneId("test-pane-3")

    // ---------------------------------------------------------------------------
    // Panes
    // ---------------------------------------------------------------------------

    fun pane(
        id: PaneId = PANE_ID_1,
        title: String = "zsh",
        isActive: Boolean = true,
    ) = ZellijPane(id = id, title = title, isActive = isActive)

    val PANE_1 = pane(id = PANE_ID_1, title = "cargo run", isActive = true)
    val PANE_2 = pane(id = PANE_ID_2, title = "tail -f server.log", isActive = false)
    val PANE_3 = pane(id = PANE_ID_3, title = "zsh", isActive = false)

    // ---------------------------------------------------------------------------
    // Tabs
    // ---------------------------------------------------------------------------

    fun tab(
        id: String = "test-tab-1",
        name: String = "backend",
        panes: List<ZellijPane> = listOf(PANE_1),
        isActive: Boolean = true,
    ) = ZellijTab(id = id, name = name, panes = panes, isActive = isActive)

    val TAB_BACKEND = tab(
        id = "tab-backend",
        name = "backend",
        panes = listOf(PANE_1, PANE_2),
        isActive = true,
    )

    val TAB_FRONTEND = tab(
        id = "tab-frontend",
        name = "frontend",
        panes = listOf(PANE_3),
        isActive = false,
    )

    // ---------------------------------------------------------------------------
    // Sessions
    // ---------------------------------------------------------------------------

    fun session(
        id: String = "test-session-1",
        name: String = "dev-server",
        tabs: List<ZellijTab> = listOf(TAB_BACKEND),
        createdAt: Instant = Instant.parse("2026-04-01T08:00:00Z"),
    ) = ZellijSession(id = id, name = name, tabs = tabs, createdAt = createdAt)

    val SESSION_DEV = session(
        id = "session-dev",
        name = "dev-server",
        tabs = listOf(TAB_BACKEND, TAB_FRONTEND),
    )

    val SESSION_STAGING = session(
        id = "session-staging",
        name = "deploy-staging",
        tabs = listOf(
            tab(id = "tab-deploy", name = "deploy", panes = listOf(PANE_1), isActive = true),
        ),
    )

    val ALL_SESSIONS = listOf(SESSION_DEV, SESSION_STAGING)

    // ---------------------------------------------------------------------------
    // PaneOutput
    // ---------------------------------------------------------------------------

    fun paneOutput(
        paneId: PaneId = PANE_ID_1,
        text: String = "$ echo hello\nhello\n",
        timestamp: Instant = Instant.parse("2026-04-01T08:01:00Z"),
    ) = PaneOutput(paneId = paneId, text = text, timestamp = timestamp)

    val PANE_OUTPUT_LINES = listOf(
        paneOutput(PANE_ID_1, "cargo build --release\n"),
        paneOutput(PANE_ID_1, "   Compiling zellij v0.3.1\n"),
        paneOutput(PANE_ID_1, "    Finished release [optimized]\n"),
    )

    // ---------------------------------------------------------------------------
    // ServerConfig
    // ---------------------------------------------------------------------------

    val SERVER_CONFIG_LOCAL = ServerConfig(
        url = "http://localhost:7800",
        authToken = null,
    )

    val SERVER_CONFIG_WITH_TOKEN = ServerConfig(
        url = "https://zellij.delo.sh",
        authToken = "test-token-abc123",
    )

    // ---------------------------------------------------------------------------
    // Transcripts
    // ---------------------------------------------------------------------------

    fun transcript(
        text: String = "list all running containers",
        confidence: Float = 0.95f,
        provider: Transcript.SpeechProvider = Transcript.SpeechProvider.MlKit,
    ) = Transcript(text = text, confidence = confidence, provider = provider)

    val TRANSCRIPT_COMMAND = transcript("docker ps --all")
    val TRANSCRIPT_TRANSCRIPTION = transcript("This is a transcription test")
}
