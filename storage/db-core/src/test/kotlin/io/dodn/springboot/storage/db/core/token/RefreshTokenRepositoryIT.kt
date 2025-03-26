package io.dodn.springboot.storage.db.core.token

import io.dodn.springboot.storage.db.CoreDbContextTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class RefreshTokenRepositoryIT : CoreDbContextTest() {

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Test
    fun testCreateAndFindToken() {
        // Given
        val userId = 1L
        val token = "test-refresh-token"
        val expiresAt = LocalDateTime.now().plusDays(7)

        val refreshToken = RefreshTokenEntity(
            token = token,
            userId = userId,
            expiresAt = expiresAt,
        )

        // When
        val savedToken = refreshTokenRepository.save(refreshToken)

        // Then
        assertThat(savedToken.id).isGreaterThan(0)
        assertThat(savedToken.token).isEqualTo(token)
        assertThat(savedToken.userId).isEqualTo(userId)
        assertThat(savedToken.expiresAt).isEqualToIgnoringNanos(expiresAt)
    }

    @Test
    fun testFindByToken() {
        // Given
        val userId = 2L
        val token = "find-test-token"
        val expiresAt = LocalDateTime.now().plusDays(7)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                token = token,
                userId = userId,
                expiresAt = expiresAt,
            ),
        )

        // When
        val foundToken = refreshTokenRepository.findByToken(token)

        // Then
        assertThat(foundToken).isPresent
        assertThat(foundToken.get().token).isEqualTo(token)
        assertThat(foundToken.get().userId).isEqualTo(userId)
    }

    @Test
    fun testFindByUserId() {
        // Given
        val userId = 3L
        val expiresAt = LocalDateTime.now().plusDays(7)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                token = "user-token-1",
                userId = userId,
                expiresAt = expiresAt,
            ),
        )

        refreshTokenRepository.save(
            RefreshTokenEntity(
                token = "user-token-2",
                userId = userId,
                expiresAt = expiresAt,
            ),
        )

        // When
        val tokens = refreshTokenRepository.findByUserId(userId)

        // Then
        assertThat(tokens).hasSize(2)
        assertThat(tokens).extracting("userId").containsOnly(userId)
    }

    @Test
    fun testDeleteAllByUserId() {
        // Given
        val userId = 4L
        val expiresAt = LocalDateTime.now().plusDays(7)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                token = "delete-token-1",
                userId = userId,
                expiresAt = expiresAt,
            ),
        )

        refreshTokenRepository.save(
            RefreshTokenEntity(
                token = "delete-token-2",
                userId = userId,
                expiresAt = expiresAt,
            ),
        )

        // When
        refreshTokenRepository.deleteAllByUserId(userId)

        // Then
        val tokens = refreshTokenRepository.findByUserId(userId)
        assertThat(tokens).isEmpty()
    }

    @Test
    fun testDeleteAllExpiredTokens() {
        // Given
        val userId = 5L
        val now = LocalDateTime.now()

        val expiredToken = refreshTokenRepository.save(
            RefreshTokenEntity(
                token = "expired-token",
                userId = userId,
                expiresAt = now.minusDays(1),
            ),
        )

        val validToken = refreshTokenRepository.save(
            RefreshTokenEntity(
                token = "valid-token",
                userId = userId,
                expiresAt = now.plusDays(1),
            ),
        )

        // When
        refreshTokenRepository.deleteAllExpiredTokens(now)

        // Then
        assertThat(refreshTokenRepository.findById(expiredToken.id)).isEmpty()
        assertThat(refreshTokenRepository.findById(validToken.id)).isPresent()
    }
}
