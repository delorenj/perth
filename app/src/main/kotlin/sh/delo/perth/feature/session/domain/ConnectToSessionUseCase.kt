package sh.delo.perth.feature.session.domain

import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.domain.repository.SessionRepository
import sh.delo.perth.core.result.AppResult
import javax.inject.Inject

/**
 * Records a session as recently visited in Room so it appears in the recent sessions list.
 * Call this when the user taps a session card to open it.
 */
class ConnectToSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(session: ZellijSession, serverUrl: String): AppResult<Unit> =
        sessionRepository.markSessionVisited(session, serverUrl)
}
