package io.dodn.springboot.core.domain.worry

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorryService(
    private val worryStorage: WorryStorage,
) {
    @Transactional
    fun createLetterWorry(worry: Worry): Worry {
        val savedWorry = worryStorage.saveWorry(worry)

        if (worry.options.isNotEmpty()) {
            worryStorage.saveWorryOptions(savedWorry.id, worry.options)
        }

        return worryStorage.getWorry(savedWorry.id)
    }

    @Transactional
    fun createConvoWorry(worry: Worry): Worry {
        val savedWorry = worryStorage.saveWorry(worry)

        if (worry.steps.isNotEmpty()) {
            worryStorage.saveWorrySteps(savedWorry.id, worry.steps)
        }

        return worryStorage.getWorry(savedWorry.id)
    }

    @Transactional(readOnly = true)
    fun getWorry(worryId: Long): Worry {
        return worryStorage.getWorry(worryId)
    }

    @Transactional
    fun createAiFeedback(worryId: Long, aiFeedback: AiFeedback): AiFeedback {
        return worryStorage.saveAiFeedback(worryId, aiFeedback)
    }
}
