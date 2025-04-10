package io.dodn.springboot.core.domain.counseling

import io.dodn.springboot.core.domain.counseling.dto.CounselingRequest
import io.dodn.springboot.core.domain.counseling.dto.CounselingResponse
import io.dodn.springboot.core.domain.counseling.dto.EmotionTagRequest
import io.dodn.springboot.core.domain.counseling.dto.EmotionTagResponse
import io.dodn.springboot.core.domain.counseling.dto.SummaryRequest
import io.dodn.springboot.core.domain.counseling.dto.SummaryResponse

interface AiCounselorClient {
    fun getCounseling(request: CounselingRequest): CounselingResponse
    fun summarizeConversation(request: SummaryRequest): SummaryResponse
    fun extractEmotionTags(request: EmotionTagRequest): EmotionTagResponse
}
