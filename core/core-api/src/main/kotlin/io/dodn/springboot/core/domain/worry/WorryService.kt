package io.dodn.springboot.core.domain.worry

import io.dodn.springboot.core.domain.worry.counselor.AiCounselorClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorryService(
    private val worryStorage: WorryStorage,
    private val aiCounselorClient: AiCounselorClient,
    private val counselorMapper: CounselorMapper,
) {
    @Transactional
    fun createLetterWorry(worry: Worry): Worry {
        val savedWorry = worryStorage.saveWorry(worry)

        if (worry.options.isNotEmpty()) {
            worryStorage.saveWorryOptions(savedWorry.id, worry.options)
        }

        return worryStorage.getWorry(savedWorry.id)
    }

    @Transactional
    fun createConvoWorry(worry: Worry): Worry {
        val savedWorry = worryStorage.saveWorry(worry)

        if (worry.steps.isNotEmpty()) {
            worryStorage.saveWorrySteps(savedWorry.id, worry.steps)
        }

        return worryStorage.getWorry(savedWorry.id)
    }

    @Transactional(readOnly = true)
    fun getWorry(worryId: Long): Worry {
        return worryStorage.getWorry(worryId)
    }

    @Transactional
    fun requestAiFeedback(worryId: Long): AiFeedback {
        val worry = worryStorage.getWorry(worryId)

        // Convert domain object to client DTOs
        val counselingRequest = counselorMapper.toRequest(worry)
        val tagRequest = counselorMapper.toEmotionTagRequest(worry)

        // Get responses from AI service
        val counselingResponse = aiCounselorClient.getCounseling(counselingRequest)
        val tagResponse = aiCounselorClient.extractEmotionTags(tagRequest)

        // Determine the tone of the feedback
        val tone = counselorMapper.determineTone(aiCounselorClient, counselingResponse.feedback)

        // Create AiFeedback from responses
        val aiFeedback = counselorMapper.toAiFeedback(counselingResponse, tagResponse, tone)

        // Save and return the feedback
        return worryStorage.saveAiFeedback(worryId, aiFeedback)
    }

    @Transactional
    fun createAiFeedback(worryId: Long, aiFeedback: AiFeedback): AiFeedback {
        return worryStorage.saveAiFeedback(worryId, aiFeedback)
    }

    @Transactional(readOnly = true)
    fun generateSummary(worryId: Long): String {
        val worry = worryStorage.getWorry(worryId)
        val summaryRequest = counselorMapper.toSummaryRequest(worry)
        val summaryResponse = aiCounselorClient.summarizeConversation(summaryRequest)
        return summaryResponse.summary
    }

    @Transactional(readOnly = true)
    fun extractEmotionTags(worryId: Long): List<String> {
        val worry = worryStorage.getWorry(worryId)
        val tagRequest = counselorMapper.toEmotionTagRequest(worry)
        val tagResponse = aiCounselorClient.extractEmotionTags(tagRequest)
        return tagResponse.tags
    }

    @Transactional
    fun updateWorrySharedStatus(worryId: Long, isShared: Boolean): Worry {
        return worryStorage.updateWorrySharedStatus(worryId, isShared)
    }
}
