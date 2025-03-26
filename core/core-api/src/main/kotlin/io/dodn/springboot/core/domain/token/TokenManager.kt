package io.dodn.springboot.core.domain.token

import io.dodn.springboot.storage.db.core.token.RefreshTokenRepository
import org.springframework.stereotype.Component

@Component
class TokenManager(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun invalidateAllTokens(userId: Long) {
        refreshTokenRepository.deleteAllByUserId(userId)
    }
}
