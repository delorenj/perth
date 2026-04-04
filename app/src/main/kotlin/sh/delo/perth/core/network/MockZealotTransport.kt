package sh.delo.perth.core.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.PaneOutput
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.model.ZellijPane
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.domain.model.ZellijTab
import sh.delo.perth.core.result.AppResult
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory mock transport that returns realistic fake data for development and testing.
 * Bound in place of [WebSocketZealotTransport] until the zealot server protocol is finalised.
 */
@Singleton
class MockZealotTransport @Inject constructor() : ZealotTransport {

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override suspend fun connect(config: ServerConfig): AppResult<Unit> {
        Timber.d("MockZealotTransport: simulating connect to url=%s", config.url)
        _connectionState.value = ConnectionState.Connecting
        delay(500)
        _connectionState.value = ConnectionState.Connected
        return AppResult.Success(Unit)
    }

    override suspend fun disconnect() {
        Timber.d("MockZealotTransport: disconnecting")
        delay(100)
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun sessionFlow(): Flow<List<ZellijSession>> = flow {
        emit(mockSessions())
    }

    override fun paneOutputFlow(paneId: PaneId): Flow<PaneOutput> =
        mockPaneOutputFlow().filter { it.paneId == paneId }

    override suspend fun sendInput(paneId: PaneId, input: String): AppResult<Unit> {
        Timber.d("MockZealotTransport: sendInput paneId=%s input=%s", paneId, input)
        delay(50)
        return AppResult.Success(Unit)
    }

    override suspend fun sendCommand(paneId: PaneId, command: String): AppResult<String> {
        Timber.d("MockZealotTransport: sendCommand paneId=%s command=%s", paneId, command)
        delay(200)
        return AppResult.Success("ok")
    }

    // ---------------------------------------------------------------------------
    // Realistic fake data
    // ---------------------------------------------------------------------------

    private fun mockSessions(): List<ZellijSession> = listOf(
        ZellijSession(
            id = "session-dev-server",
            name = "dev-server",
            tabs = listOf(
                ZellijTab(
                    id = "tab-dev-1",
                    name = "backend",
                    panes = listOf(
                        ZellijPane(
                            id = PaneId("pane-dev-1-1"),
                            title = "api: cargo run --release",
                            isActive = true,
                        ),
                        ZellijPane(
                            id = PaneId("pane-dev-1-2"),
                            title = "api: tail -f logs/server.log",
                            isActive = false,
                        ),
                    ),
                    isActive = true,
                ),
                ZellijTab(
                    id = "tab-dev-2",
                    name = "frontend",
                    panes = listOf(
                        ZellijPane(
                            id = PaneId("pane-dev-2-1"),
                            title = "web: npm run dev",
                            isActive = true,
                        ),
                    ),
                    isActive = false,
                ),
                ZellijTab(
                    id = "tab-dev-3",
                    name = "shell",
                    panes = listOf(
                        ZellijPane(
                            id = PaneId("pane-dev-3-1"),
                            title = "zsh",
                            isActive = true,
                        ),
                    ),
                    isActive = false,
                ),
            ),
            createdAt = Instant.parse("2026-04-01T08:00:00Z"),
        ),
        ZellijSession(
            id = "session-api-debug",
            name = "api-debug",
            tabs = listOf(
                ZellijTab(
                    id = "tab-debug-1",
                    name = "gdb",
                    panes = listOf(
                        ZellijPane(
                            id = PaneId("pane-debug-1-1"),
                            title = "gdb ./target/debug/zealot",
                            isActive = true,
                        ),
                        ZellijPane(
                            id = PaneId("pane-debug-1-2"),
                            title = "perf stat -- ./target/debug/zealot",
                            isActive = false,
                        ),
                    ),
                    isActive = true,
                ),
                ZellijTab(
                    id = "tab-debug-2",
                    name = "logs",
                    panes = listOf(
                        ZellijPane(
                            id = PaneId("pane-debug-2-1"),
                            title = "journalctl -fu zealot.service",
                            isActive = true,
                        ),
                    ),
                    isActive = false,
                ),
            ),
            createdAt = Instant.parse("2026-04-01T09:30:00Z"),
        ),
        ZellijSession(
            id = "session-deploy-staging",
            name = "deploy-staging",
            tabs = listOf(
                ZellijTab(
                    id = "tab-deploy-1",
                    name = "deploy",
                    panes = listOf(
                        ZellijPane(
                            id = PaneId("pane-deploy-1-1"),
                            title = "ssh deploy@staging.delo.sh",
                            isActive = true,
                        ),
                    ),
                    isActive = true,
                ),
                ZellijTab(
                    id = "tab-deploy-2",
                    name = "monitor",
                    panes = listOf(
                        ZellijPane(
                            id = PaneId("pane-deploy-2-1"),
                            title = "htop",
                            isActive = true,
                        ),
                        ZellijPane(
                            id = PaneId("pane-deploy-2-2"),
                            title = "watch -n1 docker ps",
                            isActive = false,
                        ),
                    ),
                    isActive = false,
                ),
            ),
            createdAt = Instant.parse("2026-04-01T10:15:00Z"),
        ),
    )

    private fun mockPaneOutputFlow(): Flow<PaneOutput> = flow {
        val outputs = listOf(
            PaneOutput(PaneId("pane-dev-1-1"), "   Compiling zealot-api v0.3.1 (~/code/zealot)\n"),
            PaneOutput(PaneId("pane-dev-1-1"), "    Finished release [optimized] target(s) in 4.32s\n"),
            PaneOutput(PaneId("pane-dev-1-1"), "     Running `target/release/zealot-api`\n"),
            PaneOutput(PaneId("pane-dev-1-1"), "INFO zealot_api: listening on 0.0.0.0:7800\n"),
            PaneOutput(PaneId("pane-dev-1-2"), "2026-04-01T10:15:02Z INFO  session.connected id=dev-server\n"),
            PaneOutput(PaneId("pane-dev-1-2"), "2026-04-01T10:15:05Z DEBUG pane.output pane=1 bytes=42\n"),
            PaneOutput(PaneId("pane-deploy-1-1"), "deploy@staging:~$ docker ps --format 'table {{.Names}}\\t{{.Status}}'\n"),
            PaneOutput(PaneId("pane-deploy-1-1"), "NAMES               STATUS\n"),
            PaneOutput(PaneId("pane-deploy-1-1"), "traefik             Up 3 days\n"),
            PaneOutput(PaneId("pane-deploy-1-1"), "zealot              Up 2 hours\n"),
        )
        outputs.forEach { output ->
            emit(output)
            delay(300)
        }
    }
}
