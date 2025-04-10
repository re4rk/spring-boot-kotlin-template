package io.dodn.springboot.core.domain.counseling.session

import io.dodn.springboot.storage.db.core.counseling.session.CounselingSessionRepository
import io.dodn.springboot.storage.db.core.counseling.session.CounselingVisibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CounselingSessionStore(
    private val counselingSessionRepository: CounselingSessionRepository
) {

    @Transactional
    fun save(session: CounselingSession): CounselingSession {
        val saved = counselingSessionRepository.save(session.toEntity())
        return saved.toDomain()
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): CounselingSession? {
        return counselingSessionRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    @Transactional(readOnly = true)
    fun findPublicSessions(pageable: Pageable): Page<CounselingSession> {
        return counselingSessionRepository
            .findByVisibilityNot(CounselingVisibility.PRIVATE, pageable)
            .map { it.toDomain() }
    }
}