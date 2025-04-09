package io.dodn.springboot.core.domain.counselor.session

import io.dodn.springboot.storage.db.core.counselor.session.CounselingSessionEntity
import io.dodn.springboot.storage.db.core.counselor.session.CounselingVisibility
import java.time.LocalDateTime

data class CounselingSession(
    val id: Long,
    val userId: Long?,
    val userInput: String,
    val aiResponse: String,
    val visibility: CounselingVisibility,
    val summary: String?,
    val title: String?,
    val emotionTags: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

