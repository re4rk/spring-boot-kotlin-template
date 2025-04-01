package io.dodn.springboot.storage.db.core.user

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): Optional<UserEntity>
    fun existsByEmail(email: String): Boolean
    fun findByStatus(status: UserStatus, pageable: Pageable): Page<UserEntity>

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT u FROM UserEntity u WHERE u.id = :userId")
    fun findByIdWithOptimisticLock(@Param("userId") userId: Long): Optional<UserEntity>
}
