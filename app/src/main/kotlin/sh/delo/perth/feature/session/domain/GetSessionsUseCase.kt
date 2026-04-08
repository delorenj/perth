package sh.delo.perth.feature.session.domain

import kotlinx.coroutines.flow.Flow
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Returns a [Flow] of the current live session list from the transport layer.
 * Emits whenever the zellij server pushes an updated session list.
 */
class GetSessionsUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    operator fun invoke(): Flow<List<ZellijSession>> = sessionRepository.sessionListFlow()
}
