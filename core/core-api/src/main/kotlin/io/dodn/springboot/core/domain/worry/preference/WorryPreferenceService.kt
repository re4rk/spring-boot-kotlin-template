package io.dodn.springboot.core.domain.worry.preference

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorryPreferenceService(
    private val worryPreferenceStorage: WorryPreferenceStorage,
    private val worryPreferenceProcessor: WorryPreferenceProcessor,
) {
    @Transactional
    fun createWorryPreference(userId: Long, params: WorryPreferenceParams): WorryPreference {
        return worryPreferenceProcessor.create(
            userId = userId,
            expressionStyle = params.expressionStyle,
            comfortStyle = params.comfortStyle,
        )
    }

    @Transactional
    fun getWorryPreferenceByUserId(userId: Long): WorryPreference {
        return worryPreferenceStorage.findByUserIdOrNull(userId)
            ?: worryPreferenceProcessor.createDefault(userId)
    }

    @Transactional
    fun updateWorryPreference(userId: Long, params: WorryPreferenceParams): WorryPreference {
        val preference = worryPreferenceStorage.findByUserIdOrNull(userId)
            ?: return createWorryPreference(userId, params)

        val updatedPreference = preference.copy(
            expressionStyle = params.expressionStyle,
            comfortStyle = params.comfortStyle,
        )

        return worryPreferenceProcessor.update(updatedPreference)
    }
}
