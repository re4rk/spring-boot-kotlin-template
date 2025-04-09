package io.dodn.springboot.core.domain.counselor

import io.dodn.springboot.core.domain.counselor.dto.CounselingRequest
import io.dodn.springboot.core.domain.counselor.dto.CounselingResponse
import io.dodn.springboot.core.domain.counselor.dto.EmotionTagRequest
import io.dodn.springboot.core.domain.counselor.dto.EmotionTagResponse
import io.dodn.springboot.core.domain.counselor.dto.SummaryRequest
import io.dodn.springboot.core.domain.counselor.dto.SummaryResponse

interface AiCounselorClient {
    fun getCounseling(request: CounselingRequest): CounselingResponse
    fun summarizeConversation(request: SummaryRequest): SummaryResponse
    fun extractEmotionTags(request: EmotionTagRequest): EmotionTagResponse
}
