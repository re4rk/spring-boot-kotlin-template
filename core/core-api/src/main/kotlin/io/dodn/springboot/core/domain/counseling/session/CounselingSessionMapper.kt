package io.dodn.springboot.core.domain.counseling.session

import io.dodn.springboot.storage.db.core.counseling.session.CounselingSessionEntity

fun CounselingSessionEntity.toDomain(): CounselingSession = CounselingSession(
    id = this.id,
    userId = this.userId,
    userInput = this.userInput,
    aiResponse = this.aiResponse,
    visibility = this.visibility,
    summary = this.summary,
    title = this.title,
    emotionTags = this.emotionTags,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)

fun CounselingSession.toEntity(): CounselingSessionEntity = CounselingSessionEntity(
    userId = this.userId,
    userInput = this.userInput,
    aiResponse = this.aiResponse,
    visibility = this.visibility,
    summary = this.summary,
    title = this.title,
    emotionTags = this.emotionTags,
)
