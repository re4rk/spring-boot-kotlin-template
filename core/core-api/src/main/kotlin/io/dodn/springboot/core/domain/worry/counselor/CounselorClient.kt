package io.dodn.springboot.core.domain.worry.counselor

import io.dodn.springboot.core.domain.worry.counselor.dto.CounselingRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.CounselingResponse
import io.dodn.springboot.core.domain.worry.counselor.dto.EmotionTagRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.EmotionTagResponse
import io.dodn.springboot.core.domain.worry.counselor.dto.SummaryRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.SummaryResponse

interface CounselorClient {
    fun getCounseling(request: CounselingRequest): CounselingResponse

    fun createStreamingChatCompletion(
        request: CounselingRequest,
        onChunk: (String) -> Unit,
        onComplete: (String) -> Unit,
    )

    fun summarizeConversation(request: SummaryRequest): SummaryResponse

    fun extractEmotionTags(request: EmotionTagRequest): EmotionTagResponse
}
