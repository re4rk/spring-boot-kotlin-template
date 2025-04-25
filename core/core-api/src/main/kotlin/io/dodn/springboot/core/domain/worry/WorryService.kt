package io.dodn.springboot.core.domain.worry

import io.dodn.springboot.core.domain.worry.counselor.CounselorClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorryService(
    private val worryStorage: WorryStorage,
    private val counselorClient: CounselorClient,
    private val counselorMapper: CounselorMapper,
) {
    @Transactional
    fun createWorry(worry: Worry): Worry {
        val savedWorry = worryStorage.saveWorry(worry)

        if (savedWorry.steps.isNotEmpty()) {
            worryStorage.saveWorrySteps(savedWorry.id, savedWorry.steps)
        }

        if (savedWorry.options.isNotEmpty()) {
            worryStorage.saveWorryOptions(savedWorry.id, savedWorry.options)
        }

        return worryStorage.getWorry(savedWorry.id)
    }

    @Transactional
    fun addWorryStep(worryId: Long, content: String): WorryStep {
        return worryStorage.addWorryStep(worryId, WorryStep(role = StepRole.AI, content = content, stepOrder = 1))
    }

    @Transactional(readOnly = true)
    fun getWorry(worryId: Long): Worry {
        return worryStorage.getWorry(worryId)
    }

    @Transactional
    fun requestFeedback(worryId: Long): WorryStep {
        val worry = worryStorage.getWorry(worryId)

        val counselingResponse = counselorClient.getCounseling(counselorMapper.toRequest(worry))

        return worryStorage.addWorryStep(
            worryId = worryId,
            step = WorryStep(
                role = StepRole.AI,
                content = counselingResponse.feedback,
                stepOrder = 1,
            ),
        )
    }

    fun requestStreamingFeedback(worryId: Long, onChunk: (chunk: String) -> Unit, onComplete: (String) -> Unit) {
        val worry = worryStorage.getWorry(worryId)

        val counselingRequest = counselorMapper.toRequest(worry)

        counselorClient.createStreamingChatCompletion(
            counselingRequest,
            onChunk = { chunk -> onChunk(chunk) },
            onComplete = { fullResponse -> onComplete(fullResponse) },
        )
    }

    @Transactional(readOnly = true)
    fun generateSummary(worryId: Long): String {
        val worry = worryStorage.getWorry(worryId)
        val summaryRequest = counselorMapper.toSummaryRequest(worry)
        val summaryResponse = counselorClient.summarizeConversation(summaryRequest)
        return summaryResponse.summary
    }

    @Transactional(readOnly = true)
    fun extractEmotionTags(worryId: Long): List<String> {
        val worry = worryStorage.getWorry(worryId)
        val tagRequest = counselorMapper.toEmotionTagRequest(worry)
        val tagResponse = counselorClient.extractEmotionTags(tagRequest)
        return tagResponse.tags
    }
}
