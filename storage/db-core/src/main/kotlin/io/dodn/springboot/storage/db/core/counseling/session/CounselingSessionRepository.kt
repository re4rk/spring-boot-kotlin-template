package io.dodn.springboot.storage.db.core.counseling.session

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CounselingSessionRepository : JpaRepository<CounselingSessionEntity, Long> {
    fun findByVisibilityNot(visibility: CounselingVisibility, pageable: Pageable): Page<CounselingSessionEntity>
}