package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.core.domain.worry.AiFeedback
import io.dodn.springboot.core.domain.worry.Worry
import java.time.LocalDateTime

data class Feed(
    val id: Long = 0,
    val worry: Worry,
    val feedback: AiFeedback,
    val empathyCount: Long = 0,
    val sharedAt: LocalDateTime,
)
