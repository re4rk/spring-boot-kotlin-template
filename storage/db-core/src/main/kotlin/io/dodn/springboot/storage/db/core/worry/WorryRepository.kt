package io.dodn.springboot.storage.db.core.worry

import org.springframework.data.jpa.repository.JpaRepository

interface WorryRepository : JpaRepository<WorryEntity, Long> {
    fun findByMode(worryMode: WorryMode): List<WorryEntity>
    fun findByModeAndUserId(worryMode: WorryMode, userId: Long): List<WorryEntity>
}
