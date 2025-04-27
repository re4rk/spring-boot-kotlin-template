package io.dodn.springboot.storage.db.core.worry

import org.springframework.data.jpa.repository.JpaRepository

interface WorryMessageRepository : JpaRepository<WorryMessageEntity, Long> {
    fun findByWorryIdOrderByMessageOrder(worryId: Long): List<WorryMessageEntity>
}
