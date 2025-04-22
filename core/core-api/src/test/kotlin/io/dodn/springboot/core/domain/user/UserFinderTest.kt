package io.dodn.springboot.core.domain.user

import io.dodn.springboot.UnitTest
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserRole
import io.dodn.springboot.storage.db.core.user.UserStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.Optional

class UserFinderTest : UnitTest() {
    private lateinit var userRepository: UserRepository
    private lateinit var userFinder: UserFinder

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        userFinder = UserFinder(userRepository)
    }

    @Test
    fun `should find user by id when user exists`() {
        // given
        val userId = 1L
        val mockUser = createMockUserEntity(userId, "test@example.com", UserStatus.ACTIVE)

        every { userRepository.findById(userId) } returns Optional.of(mockUser)

        // when
        val result = userFinder.findById(userId)

        // then
        assertThat(result.id).isEqualTo(userId)
        assertThat(result.email).isEqualTo("test@example.com")
        assertThat(result.status).isEqualTo(UserStatus.ACTIVE)
        verify { userRepository.findById(userId) }
    }

    @Test
    fun `should throw exception when finding user by id that doesn't exist`() {
        // given
        val userId = 1L

        every { userRepository.findById(userId) } returns Optional.empty()

        // when & then
        assertThrows<CoreException> {
            userFinder.findById(userId)
        }
    }

    @Test
    fun `should find user by email when user exists`() {
        // given
        val email = "test@example.com"
        val mockUser = createMockUserEntity(1L, email, UserStatus.ACTIVE)

        every { userRepository.findByEmail(email) } returns Optional.of(mockUser)

        // when
        val result = userFinder.findByEmail(email)

        // then
        assertThat(result.id).isEqualTo(1L)
        assertThat(result.email).isEqualTo(email)
        assertThat(result.status).isEqualTo(UserStatus.ACTIVE)
        verify { userRepository.findByEmail(email) }
    }

    @Test
    fun `should throw exception when finding user by email that doesn't exist`() {
        // given
        val email = "nonexistent@example.com"

        every { userRepository.findByEmail(email) } returns Optional.empty()

        // when & then
        assertThrows<CoreException> {
            userFinder.findByEmail(email)
        }
    }

    @Test
    fun `should find user by email and status when matching`() {
        // given
        val email = "test@example.com"
        val status = UserStatus.ACTIVE
        val mockUser = createMockUserEntity(1L, email, status)

        every { userRepository.findByEmail(email) } returns Optional.of(mockUser)

        // when
        val result = userFinder.findByEmailAndStatus(email, status)

        // then
        assertThat(result.email).isEqualTo(email)
        assertThat(result.status).isEqualTo(status)
    }

    @Test
    fun `should throw exception when finding user by email and non-matching status`() {
        // given
        val email = "test@example.com"
        val mockUser = createMockUserEntity(1L, email, UserStatus.INACTIVE)

        every { userRepository.findByEmail(email) } returns Optional.of(mockUser)

        // when & then
        assertThrows<CoreException> {
            userFinder.findByEmailAndStatus(email, UserStatus.ACTIVE)
        }
    }

    @Test
    fun `should find users by status with pagination`() {
        // given
        val status = UserStatus.ACTIVE
        val page = 0
        val size = 10
        val pageable = PageRequest.of(page, size)

        val user1 = createMockUserEntity(1L, "user1@example.com", status)
        val user2 = createMockUserEntity(2L, "user2@example.com", status)
        val mockPage = PageImpl(listOf(user1, user2), pageable, 2)

        every { userRepository.findByStatus(status, pageable) } returns mockPage

        // when
        val result = userFinder.findByStatus(status, page, size)

        // then
        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.content[0].id).isEqualTo(1L)
        assertThat(result.content[1].id).isEqualTo(2L)
        verify { userRepository.findByStatus(status, pageable) }
    }

    // Helper methods
    private fun createMockUserEntity(
        id: Long,
        email: String,
        status: UserStatus,
        name: String? = "Test User",
        role: UserRole = UserRole.USER,
    ): UserEntity {
        val user = UserEntity(
            email = email,
            password = "hashed_password",
            name = name,
            status = status,
            role = role,
        )

        // Use reflection to set the id
        val field = user.javaClass.superclass.getDeclaredField("id")
        field.isAccessible = true
        field.set(user, id)

        // Set timestamps
        val createdAtField = user.javaClass.superclass.getDeclaredField("createdAt")
        createdAtField.isAccessible = true
        createdAtField.set(user, LocalDateTime.now())

        val updatedAtField = user.javaClass.superclass.getDeclaredField("updatedAt")
        updatedAtField.isAccessible = true
        updatedAtField.set(user, LocalDateTime.now())

        return user
    }
}
