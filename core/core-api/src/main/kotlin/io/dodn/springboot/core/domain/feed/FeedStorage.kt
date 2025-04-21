package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.core.domain.worry.AiFeedback
import io.dodn.springboot.core.domain.worry.StepRole
import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode
import io.dodn.springboot.core.domain.worry.WorryOption
import io.dodn.springboot.core.domain.worry.WorryStep
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.feed.FeedEmpathyEntity
import io.dodn.springboot.storage.db.core.feed.FeedEmpathyRepository
import io.dodn.springboot.storage.db.core.feed.FeedEntity
import io.dodn.springboot.storage.db.core.feed.FeedRepository
import io.dodn.springboot.storage.db.core.worry.AiFeedbackRepository
import io.dodn.springboot.storage.db.core.worry.FeedbackTagRepository
import io.dodn.springboot.storage.db.core.worry.WorryEntity
import io.dodn.springboot.storage.db.core.worry.WorryOptionEntity
import io.dodn.springboot.storage.db.core.worry.WorryOptionRepository
import io.dodn.springboot.storage.db.core.worry.WorryRepository
import io.dodn.springboot.storage.db.core.worry.WorryStepEntity
import io.dodn.springboot.storage.db.core.worry.WorryStepRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class FeedStorage(
    private val worryRepository: WorryRepository,
    private val worryStepRepository: WorryStepRepository,
    private val worryOptionRepository: WorryOptionRepository,
    private val aiFeedbackRepository: AiFeedbackRepository,
    private val feedbackTagRepository: FeedbackTagRepository,
    private val feedRepository: FeedRepository,
    private val feedEmpathyRepository: FeedEmpathyRepository,
) {

    @Transactional
    fun saveFeed(worryId: Long, feedbackId: Long): Feed {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val aiFeedbackEntity = aiFeedbackRepository.findById(feedbackId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "AI Feedback not found") }

        val feedEntity = feedRepository.save(FeedEntity(worry = worryEntity, feedback = aiFeedbackEntity))

        return mapToFeed(feedEntity)
    }

    @Transactional(readOnly = true)
    fun getFeed(feedId: Long): Feed {
        val feedEntity = feedRepository.findById(feedId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Feed not found") }

        return mapToFeed(feedEntity)
    }

    @Transactional(readOnly = true)
    fun getAllFeeds(): List<Feed> {
        return feedRepository.findAllByOrderBySharedAtDesc().map { mapToFeed(it) }
    }

    @Transactional
    fun saveEmpathy(feedId: Long, userId: Long?): Long {
        val feedEntity = feedRepository.findById(feedId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Feed not found") }

        if (userId != null && hasUserEmpathized(feedId, userId)) {
            throw CoreException(ErrorType.DEFAULT_ERROR, "Already liked")
        }

        feedEmpathyRepository.save(FeedEmpathyEntity(feed = feedEntity, userId = userId))

        return getEmpathyCount(feedId)
    }

    @Transactional(readOnly = true)
    fun getEmpathyCount(feedId: Long): Long {
        return feedEmpathyRepository.countByFeedId(feedId)
    }

    @Transactional(readOnly = true)
    fun hasUserEmpathized(feedId: Long, userId: Long): Boolean {
        return feedEmpathyRepository.findByFeedIdAndUserId(feedId, userId) != null
    }

    @Transactional(readOnly = true)
    fun getWorry(worryId: Long): Worry {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val steps = worryStepRepository.findByWorryIdOrderByStepOrder(worryId)
        val options = worryOptionRepository.findByWorryId(worryId)

        return mapToWorry(worryEntity, steps, options)
    }

    @Transactional(readOnly = true)
    fun getAiFeedback(feedbackId: Long): AiFeedback {
        val aiFeedbackEntity = aiFeedbackRepository.findById(feedbackId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "AI Feedback not found") }

        val tags = feedbackTagRepository.findByFeedbackId(feedbackId).map { it.tag }

        return AiFeedback(
            id = aiFeedbackEntity.id,
            feedback = aiFeedbackEntity.feedback,
            tone = aiFeedbackEntity.tone,
            tags = tags,
        )
    }

    // Private mapping methods
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

    private fun mapToFeed(feedEntity: FeedEntity): Feed {
        val worry = getWorry(feedEntity.worry.id)
        val aiFeedback = getAiFeedback(feedEntity.feedback.id)
        val empathyCount = getEmpathyCount(feedEntity.id)

        return Feed(
            id = feedEntity.id,
            worry = worry,
            feedback = aiFeedback,
            empathyCount = empathyCount,
            sharedAt = feedEntity.sharedAt,
        )
    }
}
