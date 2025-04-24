package io.dodn.springboot.storage.db.core.worry

import org.springframework.data.jpa.repository.JpaRepository

interface WorryOptionRepository : JpaRepository<WorryOptionEntity, Long> {
    fun findByWorryId(worryId: Long): List<WorryOptionEntity>
}
