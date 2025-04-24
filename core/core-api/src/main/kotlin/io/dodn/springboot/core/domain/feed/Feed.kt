package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.core.domain.worry.Worry
import java.time.LocalDateTime

data class Feed(
    val id: Long = 0,
    val ownerId: Long,
    val worry: Worry,
    val content: String,
    val empathyCount: Long = 0,
    val sharedAt: LocalDateTime,
)
