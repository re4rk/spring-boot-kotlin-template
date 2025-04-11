package io.dodn.springboot.core.domain.worry

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.worry.AiFeedbackEntity
import io.dodn.springboot.storage.db.core.worry.AiFeedbackRepository
import io.dodn.springboot.storage.db.core.worry.FeedEmpathyEntity
import io.dodn.springboot.storage.db.core.worry.FeedEmpathyRepository
import io.dodn.springboot.storage.db.core.worry.FeedEntity
import io.dodn.springboot.storage.db.core.worry.FeedRepository
import io.dodn.springboot.storage.db.core.worry.FeedbackTagEntity
import io.dodn.springboot.storage.db.core.worry.FeedbackTagRepository
import io.dodn.springboot.storage.db.core.worry.WorryEntity
import io.dodn.springboot.storage.db.core.worry.WorryOptionEntity
import io.dodn.springboot.storage.db.core.worry.WorryOptionRepository
import io.dodn.springboot.storage.db.core.worry.WorryRepository
import io.dodn.springboot.storage.db.core.worry.WorryStepEntity
import io.dodn.springboot.storage.db.core.worry.WorryStepRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import io.dodn.springboot.storage.db.core.worry.StepRole as DbStepRole
import io.dodn.springboot.storage.db.core.worry.WorryMode as DbWorryMode

@Service
class WorryService(
    private val worryRepository: WorryRepository,
    private val worryStepRepository: WorryStepRepository,
    private val worryOptionRepository: WorryOptionRepository,
    private val aiFeedbackRepository: AiFeedbackRepository,
    private val feedbackTagRepository: FeedbackTagRepository,
    private val feedRepository: FeedRepository,
    private val feedEmpathyRepository: FeedEmpathyRepository,
) {
    @Transactional
    fun createLetterWorry(worry: Worry): Worry {
        val worryEntity = worryRepository.save(
            WorryEntity(
                userId = worry.userId,
                mode = DbWorryMode.valueOf(worry.mode.name),
                emotion = worry.emotion,
                category = worry.category,
                content = worry.content,
                isShared = worry.isShared,
            ),
        )

        val options = worry.options.map { option ->
            worryOptionRepository.save(
                WorryOptionEntity(
                    worry = worryEntity,
                    label = option.label,
                    text = option.text,
                ),
            )
        }

        return mapToWorry(worryEntity, emptyList(), options)
    }

    @Transactional
    fun createConvoWorry(worry: Worry): Worry {
        val worryEntity = worryRepository.save(
            WorryEntity(
                userId = worry.userId,
                mode = DbWorryMode.valueOf(worry.mode.name),
                emotion = worry.emotion,
                category = worry.category,
                content = "",
                isShared = worry.isShared,
            ),
        )

        val steps = worry.steps.map { step ->
            worryStepRepository.save(
                WorryStepEntity(
                    worry = worryEntity,
                    role = DbStepRole.valueOf(step.role.name),
                    content = step.content,
                    stepOrder = step.stepOrder,
                ),
            )
        }

        return mapToWorry(worryEntity, steps, emptyList())
    }

    @Transactional(readOnly = true)
    fun getWorry(worryId: Long): Worry {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val steps = worryStepRepository.findByWorryIdOrderByStepOrder(worryId)
        val options = worryOptionRepository.findByWorryId(worryId)

        return mapToWorry(worryEntity, steps, options)
    }

    @Transactional
    fun createAiFeedback(worryId: Long, aiFeedback: AiFeedback): AiFeedback {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val aiFeedbackEntity = aiFeedbackRepository.save(
            AiFeedbackEntity(
                worry = worryEntity,
                feedback = aiFeedback.feedback,
                tone = aiFeedback.tone,
            ),
        )

        val tags = aiFeedback.tags.map { tag ->
            feedbackTagRepository.save(
                FeedbackTagEntity(
                    feedback = aiFeedbackEntity,
                    tag = tag,
                ),
            )
        }

        return AiFeedback(
            feedback = aiFeedbackEntity.feedback,
            tone = aiFeedbackEntity.tone,
            tags = tags.map { it.tag },
        )
    }

    @Transactional
    fun shareWorry(worryId: Long, aiFeedbackId: Long): Feed {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val aiFeedbackEntity = aiFeedbackRepository.findById(aiFeedbackId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "AI Feedback not found") }

        // Update worry isShared
        worryEntity.isShared = true
        worryRepository.save(worryEntity)

        val feedEntity = feedRepository.save(
            FeedEntity(
                worry = worryEntity,
                feedback = aiFeedbackEntity,
            ),
        )

        val worry = getWorry(worryId)
        val aiFeedback = mapToAiFeedback(aiFeedbackEntity)

        return Feed(
            worry = worry,
            feedback = aiFeedback,
        )
    }

    @Transactional(readOnly = true)
    fun getFeeds(emotion: String? = null, tag: String? = null): List<Feed> {
        val feeds = feedRepository.findAllByOrderBySharedAtDesc()

        val result = feeds.map { feedEntity ->
            val worry = getWorry(feedEntity.worry.id)
            val aiFeedback = mapToAiFeedback(feedEntity.feedback)
            val empathyCount = feedEmpathyRepository.countByFeedId(feedEntity.id)

            Feed(
                worry = worry,
                feedback = aiFeedback,
                empathyCount = empathyCount,
            )
        }

        return result.filter { feed ->
            (emotion == null || feed.worry.emotion == emotion) &&
                (tag == null || feed.feedback.tags.contains(tag))
        }
    }

    @Transactional(readOnly = true)
    fun getFeed(feedId: Long): Feed {
        val feedEntity = feedRepository.findById(feedId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Feed not found") }

        val worry = getWorry(feedEntity.worry.id)
        val aiFeedback = mapToAiFeedback(feedEntity.feedback)
        val empathyCount = feedEmpathyRepository.countByFeedId(feedId)

        return Feed(
            worry = worry,
            feedback = aiFeedback,
            empathyCount = empathyCount,
        )
    }

    @Transactional
    fun addEmpathy(feedId: Long, userId: Long?): Long {
        val feedEntity = feedRepository.findById(feedId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Feed not found") }

        if (userId != null) {
            val existing = feedEmpathyRepository.findByFeedIdAndUserId(feedId, userId)
            if (existing != null) {
                throw CoreException(ErrorType.DEFAULT_ERROR, "Already liked")
            }
        }

        feedEmpathyRepository.save(
            FeedEmpathyEntity(
                feed = feedEntity,
                userId = userId,
            ),
        )

        return feedEmpathyRepository.countByFeedId(feedId)
    }

    private fun mapToWorry(
        worryEntity: WorryEntity,
        stepEntities: List<WorryStepEntity>,
        optionEntities: List<WorryOptionEntity>,
    ): Worry {
        val steps = stepEntities.map { step ->
            WorryStep(
                id = step.id,
                role = StepRole.valueOf(step.role.name),
                content = step.content,
                stepOrder = step.stepOrder,
            )
        }

        val options = optionEntities.map { option ->
            WorryOption(
                id = option.id,
                label = option.label,
                text = option.text,
            )
        }

        return Worry(
            id = worryEntity.id,
            userId = worryEntity.userId,
            mode = WorryMode.valueOf(worryEntity.mode.name),
            emotion = worryEntity.emotion,
            category = worryEntity.category,
            content = worryEntity.content,
            isShared = worryEntity.isShared,
            steps = steps,
            options = options,
        )
    }

    private fun mapToAiFeedback(aiFeedbackEntity: AiFeedbackEntity): AiFeedback {
        val tags = feedbackTagRepository.findByFeedbackId(aiFeedbackEntity.id).map { it.tag }

        return AiFeedback(
            feedback = aiFeedbackEntity.feedback,
            tone = aiFeedbackEntity.tone,
            tags = tags,
        )
    }
}
