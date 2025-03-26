package io.dodn.springboot.storage.db.core.user

import io.dodn.springboot.storage.db.CoreDbContextTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class UserRepositoryIT : CoreDbContextTest() {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun testCreateAndFindUser() {
        // Given
        val email = "test@example.com"
        val password = "encoded_password"
        val name = "Test User"

        val user = UserEntity(
            email = email,
            password = password,
            name = name,
        )

        // When
        val savedUser = userRepository.save(user)

        // Then
        assertThat(savedUser.id).isGreaterThan(0)
        assertThat(savedUser.email).isEqualTo(email)
        assertThat(savedUser.password).isEqualTo(password)
        assertThat(savedUser.name).isEqualTo(name)
        assertThat(savedUser.status).isEqualTo(UserStatus.ACTIVE)
        assertThat(savedUser.role).isEqualTo(UserRole.USER)
        assertThat(savedUser.createdAt).isNotNull()
        assertThat(savedUser.updatedAt).isNotNull()
    }

    @Test
    fun testFindByEmail() {
        // Given
        val email = "find-test@example.com"
        val password = "encoded_password"

        val user = UserEntity(
            email = email,
            password = password,
        )

        userRepository.save(user)

        // When
        val foundUser = userRepository.findByEmail(email)

        // Then
        assertThat(foundUser).isPresent
        assertThat(foundUser.get().email).isEqualTo(email)
    }

    @Test
    fun testExistsByEmail() {
        // Given
        val email = "exists-test@example.com"
        val password = "encoded_password"

        val user = UserEntity(
            email = email,
            password = password,
        )

        userRepository.save(user)

        // When & Then
        assertThat(userRepository.existsByEmail(email)).isTrue
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse
    }

    @Test
    fun testUpdateLastLoginAt() {
        // Given
        val email = "last-login-test@example.com"
        val password = "encoded_password"

        val user = UserEntity(
            email = email,
            password = password,
        )

        val savedUser = userRepository.save(user)
        assertThat(savedUser.lastLoginAt).isNull()

        // When
        val loginTime = LocalDateTime.now()
        savedUser.lastLoginAt = loginTime
        userRepository.save(savedUser)

        // Then
        val updatedUser = userRepository.findByEmail(email).get()
        assertThat(updatedUser.lastLoginAt).isNotNull()
        assertThat(updatedUser.lastLoginAt).isEqualToIgnoringNanos(loginTime)
    }

    @Test
    fun testUpdateUserStatus() {
        // Given
        val email = "status-test@example.com"
        val password = "encoded_password"

        val user = UserEntity(
            email = email,
            password = password,
        )

        val savedUser = userRepository.save(user)
        assertThat(savedUser.status).isEqualTo(UserStatus.ACTIVE)

        // When
        savedUser.status = UserStatus.LOCKED
        userRepository.save(savedUser)

        // Then
        val updatedUser = userRepository.findByEmail(email).get()
        assertThat(updatedUser.status).isEqualTo(UserStatus.LOCKED)
    }
}
