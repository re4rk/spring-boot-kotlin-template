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
    fun createLetterWorry(worry: Worry): Worry {
        val savedWorry = worryStorage.saveWorry(worry)

        if (savedWorry.options.isNotEmpty()) {
            worryStorage.saveWorryOptions(savedWorry.id, savedWorry.options)
        }

        return worryStorage.getWorry(savedWorry.id)
    }

    @Transactional
    fun createConvoWorry(worry: Worry): Worry {
        val savedWorry = worryStorage.saveWorry(worry)

        if (savedWorry.steps.isNotEmpty()) {
            worryStorage.saveWorrySteps(savedWorry.id, savedWorry.steps)
        }

        return worryStorage.getWorry(savedWorry.id)
    }

    @Transactional(readOnly = true)
    fun getWorry(worryId: Long): Worry {
        return worryStorage.getWorry(worryId)
    }

    @Transactional
    fun requestFeedback(worryId: Long): Feedback {
        val worry = worryStorage.getWorry(worryId)

        val counselingResponse = counselorClient.getCounseling(counselorMapper.toRequest(worry))
        val tagResponse = counselorClient.extractEmotionTags(counselorMapper.toEmotionTagRequest(worry))

        val tone = counselorMapper.determineTone(counselorClient, counselingResponse.feedback)

        return worryStorage.saveFeedback(
            worryId = worryId,
            feedback = counselorMapper.toFeedback(counselingResponse, tagResponse, tone),
        )
    }

    @Transactional
    fun createFeedback(worryId: Long, feedback: Feedback): Feedback {
        return worryStorage.saveFeedback(worryId, feedback)
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
