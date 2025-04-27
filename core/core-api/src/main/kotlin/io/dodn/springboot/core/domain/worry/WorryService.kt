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

        if (savedWorry.messages.isNotEmpty()) {
            worryStorage.saveWorryMessages(savedWorry.id, savedWorry.messages)
        }

        if (savedWorry.options.isNotEmpty()) {
            worryStorage.saveWorryOptions(savedWorry.id, savedWorry.options)
        }

        return worryStorage.getWorry(savedWorry.id)
    }

    @Transactional
    fun addWorryMessage(worryId: Long, role: MessageRole, content: String): WorryMessage {
        val worry = worryStorage.getWorry(worryId)
        return worryStorage.addWorryMessage(
            worryId,
            WorryMessage(role = role, content = content, messageOrder = worry.lastMessageOrder + 1),
        )
    }

    @Transactional(readOnly = true)
    fun getWorry(worryId: Long): Worry {
        return worryStorage.getWorry(worryId)
    }

    @Transactional
    fun requestFeedback(worryId: Long): WorryMessage {
        val worry = worryStorage.getWorry(worryId)

        val counselingResponse = counselorClient.getCounseling(counselorMapper.toRequest(worry))

        return worryStorage.addWorryMessage(
            worryId = worryId,
            WorryMessage(role = MessageRole.AI, content = counselingResponse.feedback, messageOrder = worry.lastMessageOrder + 1),
        )
    }

    fun requestStreamingFeedback(worryId: Long, onChunk: (chunk: String) -> Unit, onComplete: (String) -> Unit) {
        val worry = worryStorage.getWorry(worryId)

        val counselingRequest = counselorMapper.toRequest(worry)

        counselorClient.createStreamingChatCompletion(counselingRequest, onChunk = onChunk, onComplete = onComplete)
    }

    @Transactional(readOnly = true)
    fun generateSummary(worryId: Long): String {
        val worry = worryStorage.getWorry(worryId)
        val summaryRequest = counselorMapper.toSummaryRequest(worry)
        val summaryResponse = counselorClient.summarizeConversation(summaryRequest)
        return summaryResponse.summary
    }
}
