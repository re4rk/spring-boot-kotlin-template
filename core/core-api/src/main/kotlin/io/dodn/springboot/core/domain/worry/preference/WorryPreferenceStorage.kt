package io.dodn.springboot.core.domain.worry.preference

import io.dodn.springboot.storage.db.core.worry.preference.DbComfortStyle
import io.dodn.springboot.storage.db.core.worry.preference.DbExpressionStyle
import io.dodn.springboot.storage.db.core.worry.preference.WorryPreferenceEntity
import io.dodn.springboot.storage.db.core.worry.preference.WorryPreferenceEntityRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class WorryPreferenceStorage(
    private val worryPreferenceEntityRepository: WorryPreferenceEntityRepository,
) {
    @Transactional(readOnly = true)
    fun findByUserIdOrNull(userId: Long): WorryPreference? {
        return worryPreferenceEntityRepository.findByUserId(userId)?.toDomain()
    }

    fun findByUserId(userId: Long): WorryPreference? {
        return worryPreferenceEntityRepository.findByUserId(userId)?.toDomain()
    }

    fun save(preference: WorryPreference): WorryPreference {
        val entity = preference.toEntity()
        return worryPreferenceEntityRepository.save(entity).toDomain()
    }

    private fun WorryPreferenceEntity.toDomain(): WorryPreference {
        return WorryPreference(
            id = this.id,
            userId = this.userId,
            expressionStyle = ExpressionStyle.valueOf(this.expressionStyle.name),
            comfortStyle = ComfortStyle.valueOf(this.comfortStyle.name),
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
    }

    private fun WorryPreference.toEntity(): WorryPreferenceEntity {
        return WorryPreferenceEntity(
            id = this.id,
            userId = this.userId,
            expressionStyle = DbExpressionStyle.valueOf(this.expressionStyle.name),
            comfortStyle = DbComfortStyle.valueOf(this.comfortStyle.name),
            createdAt = this.createdAt ?: LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}
