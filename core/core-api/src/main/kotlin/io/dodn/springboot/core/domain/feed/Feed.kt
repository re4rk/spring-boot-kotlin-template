package io.dodn.springboot.core.domain.feed

import java.time.LocalDateTime

data class Feed(
    val id: Long = 0,
    val ownerId: Long,
    val emotion: String,
    val content: String,
    val empathyCount: Long = 0,
    val sharedAt: LocalDateTime,
)
