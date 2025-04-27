package io.dodn.springboot.core.domain.worry

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.worry.WorryEntity
import io.dodn.springboot.storage.db.core.worry.WorryOptionEntity
import io.dodn.springboot.storage.db.core.worry.WorryOptionRepository
import io.dodn.springboot.storage.db.core.worry.WorryRepository
import io.dodn.springboot.storage.db.core.worry.WorryMessageEntity
import io.dodn.springboot.storage.db.core.worry.WorryMessageRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import io.dodn.springboot.storage.db.core.worry.DbMessageRole
import io.dodn.springboot.storage.db.core.worry.DbWorryMode

@Component
class WorryStorage(
    private val worryRepository: WorryRepository,
    private val worryMessageRepository: WorryMessageRepository,
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

        val messages = worryMessageRepository.findByWorryIdOrderByMessageOrder(worryId)
        val options = worryOptionRepository.findByWorryId(worryId)

        return mapToWorry(worryEntity, messages, options)
    }

    @Transactional
    fun saveWorryMessages(worryId: Long, messages: List<WorryMessage>): List<WorryMessage> {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val messageEntities = messages.map { message ->
            worryMessageRepository.save(
                WorryMessageEntity(
                    worry = worryEntity,
                    role = DbMessageRole.valueOf(message.role.name),
                    content = message.content,
                    messageOrder = message.messageOrder,
                ),
            )
        }

        return messageEntities.map { entity ->
            WorryMessage(
                id = entity.id,
                role = MessageRole.valueOf(entity.role.name),
                content = entity.content,
                messageOrder = entity.messageOrder,
            )
        }
    }

    @Transactional
    fun addWorryMessage(worryId: Long, message: WorryMessage): WorryMessage {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val messageEntity = worryMessageRepository.save(
            WorryMessageEntity(
                worry = worryEntity,
                role = DbMessageRole.valueOf(message.role.name),
                content = message.content,
                messageOrder = message.messageOrder,
            ),
        )

        return WorryMessage(
            id = messageEntity.id,
            role = MessageRole.valueOf(messageEntity.role.name),
            content = messageEntity.content,
            messageOrder = messageEntity.messageOrder,
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
        messageEntities: List<WorryMessageEntity>,
        optionEntities: List<WorryOptionEntity>,
    ): Worry {
        val messages = messageEntities.map { message ->
            WorryMessage(
                id = message.id,
                role = MessageRole.valueOf(message.role.name),
                content = message.content,
                messageOrder = message.messageOrder,
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
            lastMessageOrder = messages.maxOfOrNull { it.messageOrder } ?: 0,
            messages = messages,
            options = options,
        )
    }
}
