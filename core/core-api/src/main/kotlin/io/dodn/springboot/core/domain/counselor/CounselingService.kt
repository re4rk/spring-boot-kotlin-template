package io.dodn.springboot.core.domain.counselor

import io.dodn.springboot.core.domain.counselor.dto.*
import io.dodn.springboot.core.domain.counselor.session.CounselingSession
import io.dodn.springboot.core.domain.counselor.session.CounselingSessionStore
import io.dodn.springboot.storage.db.core.counselor.session.CounselingVisibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CounselingService(
    private val aiCounselorClient: AiCounselorClient,
    private val sessionStore: CounselingSessionStore,
) {

    @Transactional
    fun handleCounseling(request: CounselingRequest, userId: Long?): CounselingResponse {
        val aiResponse = aiCounselorClient.getCounseling(request)

        val session = CounselingSession(
            id = 0L,
            userId = userId,
            userInput = request.userInput,
            aiResponse = aiResponse.aiResponse,
            visibility = CounselingVisibility.PRIVATE,
            summary = null,
            title = null,
            emotionTags = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        sessionStore.save(session)
        return aiResponse
    }

    @Transactional
    fun updateSummaryAndTags(sessionId: Long): SummaryResponse {
        val session = sessionStore.findById(sessionId) ?: error("Session not found")

        val summaryResponse = aiCounselorClient.summarizeConversation(
            SummaryRequest(fullConversation = session.userInput + "\n" + session.aiResponse),
        )
        val emotionTagResponse = aiCounselorClient.extractEmotionTags(
            EmotionTagRequest(fullConversation = session.userInput + "\n" + session.aiResponse),
        )

        val updated = session.copy(
            summary = summaryResponse.summary,
            title = summaryResponse.title,
            emotionTags = emotionTagResponse.tags,
            updatedAt = LocalDateTime.now(),
        )

        sessionStore.save(updated)
        return summaryResponse
    }

    @Transactional(readOnly = true)
    fun getSessionById(sessionId: Long): CounselingSession? {
        return sessionStore.findById(sessionId)
    }

    @Transactional(readOnly = true)
    fun getPublicFeed(pageable: Pageable): Page<CounselingSession> {
        return sessionStore.findPublicSessions(pageable)
    }

    @Transactional
    fun updateVisibility(sessionId: Long, visibility: CounselingVisibility): Boolean {
        val session = sessionStore.findById(sessionId) ?: return false
        val updated = session.copy(
            visibility = visibility,
            updatedAt = LocalDateTime.now(),
        )
        sessionStore.save(updated)
        return true
    }
}
