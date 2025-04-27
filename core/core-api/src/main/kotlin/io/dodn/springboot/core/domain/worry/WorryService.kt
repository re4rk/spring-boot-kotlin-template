package io.dodn.springboot.core.domain.worry

import io.dodn.springboot.core.domain.worry.counselor.CounselorClient
import io.dodn.springboot.core.domain.worry.counselor.dto.SummaryRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Service
class WorryService(
    private val worryStorage: WorryStorage,
    private val counselorClient: CounselorClient,
    private val counselorMapper: CounselorMapper,
    @Autowired private val transactionManager: PlatformTransactionManager,
    @Qualifier("applicationTaskExecutor") private val executor: Executor,
) {
    private val logger = LoggerFactory.getLogger(WorryService::class.java)

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
            WorryMessage(
                role = MessageRole.AI,
                content = counselingResponse.feedback,
                messageOrder = worry.lastMessageOrder + 1,
            ),
        )
    }

    fun requestStreamingFeedback(worryId: Long, onChunk: (chunk: String) -> Unit, onComplete: (String) -> Unit) {
        val worry = worryStorage.getWorry(worryId)

        val counselingRequest = counselorMapper.toRequest(worry)

        counselorClient.createStreamingChatCompletion(counselingRequest, onChunk = onChunk, onComplete = onComplete)
    }

    @Transactional(readOnly = true)
    fun generateSummary(worryId: Long) {
        val worry = worryStorage.getWorry(worryId)
        val summaryRequest = counselorMapper.toSummaryRequest(worry)

        generateSummaryInner(worryId, summaryRequest)

        return
    }

    private fun generateSummaryInner(
        worryId: Long,
        summaryRequest: SummaryRequest,
    ): CompletableFuture<Void> {
        return CompletableFuture.supplyAsync({
            logger.info("Starting async summary generation for worry $worryId")

            // Request the summary from the counselor client
            val summaryResponse = try {
                counselorClient.summarizeConversation(summaryRequest)
            } catch (e: Exception) {
                logger.error("Error during summary generation for worry $worryId", e)
                throw e
            }

            // save the summary to the database
            val transactionTemplate = TransactionTemplate(transactionManager)

            transactionTemplate.execute { status ->
                try {
                    worryStorage.saveWorrySummary(worryId, summaryResponse.summary)
                    logger.info("Successfully generated and saved summary for worry $worryId")
                } catch (e: Exception) {
                    logger.error("Failed to save summary for worry $worryId", e)
                    status.setRollbackOnly()
                    throw e
                }
            }

            null
        }, executor)
    }
}
