package io.dodn.springboot.storage.db.core.worry

import org.springframework.data.jpa.repository.JpaRepository

interface WorryStepRepository : JpaRepository<WorryMessageEntity, Long> {
    fun findByWorryIdOrderByStepOrder(worryId: Long): List<WorryMessageEntity>
}
