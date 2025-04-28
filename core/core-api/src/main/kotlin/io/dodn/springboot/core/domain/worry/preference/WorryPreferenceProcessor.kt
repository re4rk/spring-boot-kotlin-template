package io.dodn.springboot.core.domain.worry.preference

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class WorryPreferenceProcessor(
    private val worryPreferenceStorage: WorryPreferenceStorage,
) {

    @Transactional
    fun create(userId: Long, expressionStyle: ExpressionStyle, comfortStyle: ComfortStyle): WorryPreference {
        // 이미 존재하는지 확인
        if (worryPreferenceStorage.findByUserId(userId) != null) {
            throw CoreException(ErrorType.DUPLICATE_RESOURCE, "User already has worry preference settings")
        }

        val now = LocalDateTime.now()
        val preference = WorryPreference(
            id = 0L,
            userId = userId,
            expressionStyle = expressionStyle,
            comfortStyle = comfortStyle,
            createdAt = now,
            updatedAt = now,
        )

        return worryPreferenceStorage.save(preference)
    }

    @Transactional
    fun createDefault(userId: Long): WorryPreference {
        val now = LocalDateTime.now()
        val defaultPreference = WorryPreference(
            id = 0L,
            userId = userId,
            expressionStyle = ExpressionStyle.DIRECT,
            comfortStyle = ComfortStyle.SUPPORTIVE,
            createdAt = now,
            updatedAt = now,
        )

        return worryPreferenceStorage.save(defaultPreference)
    }

    @Transactional
    fun update(preference: WorryPreference): WorryPreference {
        val updatedPreference = preference.copy(updatedAt = LocalDateTime.now())
        return worryPreferenceStorage.save(updatedPreference)
    }
}
