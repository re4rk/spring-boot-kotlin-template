package io.dodn.springboot.core.domain.counseling.session

import io.dodn.springboot.storage.db.core.counseling.session.CounselingVisibility
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

