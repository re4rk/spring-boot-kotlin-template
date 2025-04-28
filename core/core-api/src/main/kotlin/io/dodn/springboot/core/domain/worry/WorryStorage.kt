package io.dodn.springboot.core.domain.worry

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.worry.DbMessageRole
import io.dodn.springboot.storage.db.core.worry.DbWorryMode
import io.dodn.springboot.storage.db.core.worry.WorryEntity
import io.dodn.springboot.storage.db.core.worry.WorryMessageEntity
import io.dodn.springboot.storage.db.core.worry.WorryMessageRepository
import io.dodn.springboot.storage.db.core.worry.WorryOptionEntity
import io.dodn.springboot.storage.db.core.worry.WorryOptionRepository
import io.dodn.springboot.storage.db.core.worry.WorryRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

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

        return worryEntity.toWorry()
    }

    @Transactional(readOnly = true)
    fun getWorry(worryId: Long): Worry {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        return worryEntity.toWorry()
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

        return messageEntities.map { it.toWorryMessage() }
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

        return messageEntity.toWorryMessage()
    }

    @Transactional
    fun saveWorryOptions(worryId: Long, options: List<WorryOption>): List<WorryOption> {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        val optionEntities = options.map { option ->
            worryOptionRepository.save(
                WorryOptionEntity(worry = worryEntity, label = option.label, text = option.text),
            )
        }

        return optionEntities.map { entity -> entity.toWorryOption() }
    }

    @Transactional
    fun saveWorrySummary(worryId: Long, summary: String): Worry {
        val worryEntity = worryRepository.findById(worryId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Worry not found") }

        worryEntity.content = summary

        return worryEntity.toWorry()
    }

    private fun WorryEntity.toWorry(): Worry {
        val messages = worryMessageRepository.findByWorryIdOrderByMessageOrder(this.id)
            .map { message -> message.toWorryMessage() }

        val options = worryOptionRepository.findByWorryId(this.id).map { option -> option.toWorryOption() }

        return Worry(
            id = this.id,
            userId = this.userId,
            mode = WorryMode.valueOf(this.mode.name),
            emotion = this.emotion,
            category = this.category,
            content = this.content,
            lastMessageOrder = messages.maxOfOrNull { it.messageOrder } ?: 0,
            messages = messages,
            options = options,
        )
    }

    private fun WorryOptionEntity.toWorryOption(): WorryOption {
        return WorryOption(
            id = this.id,
            label = this.label,
            text = this.text,
        )
    }

    private fun WorryMessageEntity.toWorryMessage(): WorryMessage {
        return WorryMessage(
            id = this.id,
            role = MessageRole.valueOf(this.role.name),
            content = this.content,
            messageOrder = this.messageOrder,
        )
    }
}
