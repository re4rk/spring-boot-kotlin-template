package io.dodn.springboot.core.domain.worry

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.worry.WorryEntity
import io.dodn.springboot.storage.db.core.worry.WorryOptionEntity
import io.dodn.springboot.storage.db.core.worry.WorryOptionRepository
import io.dodn.springboot.storage.db.core.worry.WorryRepository
import io.dodn.springboot.storage.db.core.worry.WorryMessageEntity
import io.dodn.springboot.storage.db.core.worry.WorryStepRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import io.dodn.springboot.storage.db.core.worry.StepRole as DbStepRole
import io.dodn.springboot.storage.db.core.worry.WorryMode as DbWorryMode

@Component
class WorryStorage(
    private val worryRepository: WorryRepository,
    private val worryMessageRepository: WorryStepRepository,
    private val worryOptionRepository: WorryOptionRepository,
) {

    @Transactional
    fun saveWorry(worry: Worry): Worry {
        val worryEntity = worryRepository.save(
            WorryEntity(
                userId = worry.userId,
                mode = DbWorryMode.valueOf(worry.mode.name),
                emotion = worry.emotion,
                category = worry.category,
                content = worry.content,
            ),
        )

        return mapToWorry(worryEntity, emptyList(), emptyList())
    }

    @Transactional(readOnly = true)
    fun getWorry(worryId: Long): Worry {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val steps = worryMessageRepository.findByWorryIdOrderByStepOrder(worryId)
        val options = worryOptionRepository.findByWorryId(worryId)

        return mapToWorry(worryEntity, steps, options)
    }

    @Transactional
    fun saveWorrySteps(worryId: Long, steps: List<WorryMessage>): List<WorryMessage> {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val stepEntities = steps.map { step ->
            worryMessageRepository.save(
                WorryMessageEntity(
                    worry = worryEntity,
                    role = DbStepRole.valueOf(step.role.name),
                    content = step.content,
                    messageOrder = step.messageOrder,
                ),
            )
        }

        return stepEntities.map { entity ->
            WorryMessage(
                id = entity.id,
                role = StepRole.valueOf(entity.role.name),
                content = entity.content,
                messageOrder = entity.messageOrder,
            )
        }
    }

    @Transactional
    fun addWorryMessage(worryId: Long, step: WorryMessage): WorryMessage {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val stepEntity = worryMessageRepository.save(
            WorryMessageEntity(
                worry = worryEntity,
                role = DbStepRole.valueOf(step.role.name),
                content = step.content,
                messageOrder = step.messageOrder,
            ),
        )

        return WorryMessage(
            id = stepEntity.id,
            role = StepRole.valueOf(stepEntity.role.name),
            content = stepEntity.content,
            messageOrder = stepEntity.messageOrder,
        )
    }

    @Transactional
    fun saveWorryOptions(worryId: Long, options: List<WorryOption>): List<WorryOption> {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val optionEntities = options.map { option ->
            worryOptionRepository.save(
                WorryOptionEntity(
                    worry = worryEntity,
                    label = option.label,
                    text = option.text,
                ),
            )
        }

        return optionEntities.map { entity ->
            WorryOption(
                id = entity.id,
                label = entity.label,
                text = entity.text,
            )
        }
    }

    private fun mapToWorry(
        worryEntity: WorryEntity,
        stepEntities: List<WorryMessageEntity>,
        optionEntities: List<WorryOptionEntity>,
    ): Worry {
        val steps = stepEntities.map { step ->
            WorryMessage(
                id = step.id,
                role = StepRole.valueOf(step.role.name),
                content = step.content,
                messageOrder = step.messageOrder,
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
            lastStepOrder = steps.maxOfOrNull { it.messageOrder } ?: 0,
            steps = steps,
            options = options,
        )
    }
}
