package io.dodn.springboot.core.domain.worry.counselor

import io.dodn.springboot.core.domain.worry.counselor.dto.CounselingRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.CounselingResponse
import io.dodn.springboot.core.domain.worry.counselor.dto.EmotionTagRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.EmotionTagResponse
import io.dodn.springboot.core.domain.worry.counselor.dto.SummaryRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.SummaryResponse

/**
 * Interface for AI counseling services
 * Domain agnostic client interface
 */
interface CounselorClient {
    /**
     * Generate AI counseling feedback
     */
    fun getCounseling(request: CounselingRequest): CounselingResponse

    /**
     * Generate a summary for a conversation
     */
    fun summarizeConversation(request: SummaryRequest): SummaryResponse

    /**
     * Extract emotion tags from content
     */
    fun extractEmotionTags(request: EmotionTagRequest): EmotionTagResponse
}
