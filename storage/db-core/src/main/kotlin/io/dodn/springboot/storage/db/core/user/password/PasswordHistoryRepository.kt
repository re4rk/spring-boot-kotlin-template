package io.dodn.springboot.storage.db.core.user.password

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PasswordHistoryRepository : JpaRepository<PasswordHistoryEntity, Long> {
    @Query("SELECT p FROM PasswordHistoryEntity p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    fun findRecentPasswordsByUserId(@Param("userId") userId: Long, pageable: Pageable): List<PasswordHistoryEntity>
}
