package io.dodn.springboot.core.domain.user

import io.dodn.springboot.UnitTest
import io.dodn.springboot.core.domain.token.TokenManager
import io.dodn.springboot.core.domain.user.password.PasswordResetManager
import io.dodn.springboot.core.domain.user.password.PasswordResetVerifyResult
import io.dodn.springboot.storage.db.core.user.UserStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import java.time.LocalDateTime

class UserServiceTest : UnitTest() {
    private lateinit var userFinder: UserFinder
    private lateinit var userPasswordManager: UserPasswordManager
    private lateinit var userStateProcessor: UserStateProcessor
    private lateinit var tokenManager: TokenManager
    private lateinit var passwordResetManager: PasswordResetManager
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userFinder = mockk(relaxed = true)
        userPasswordManager = mockk(relaxed = true)
        userStateProcessor = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        passwordResetManager = mockk(relaxed = true)
        userService = UserService(
            userFinder,
            userPasswordManager,
            userStateProcessor,
            tokenManager,
            passwordResetManager,
        )
    }

    @Test
    fun `should find user by email`() {
        // given
        val email = "test@example.com"
        val mockUserInfo = createMockUserInfo(1L, email)

        every { userFinder.findByEmail(email) } returns mockUserInfo

        // when
        val result = userService.findByEmail(email)

        // then
        assertThat(result).isEqualTo(mockUserInfo)
        verify { userFinder.findByEmail(email) }
    }

    @Test
    fun `should verify credentials and update last login`() {
        // given
        val email = "test@example.com"
        val password = "password123"
        val userId = 1L
        val mockUserInfo = createMockUserInfo(userId, email)
        val updatedUserInfo = mockUserInfo.copy(lastLoginAt = LocalDateTime.now())

        every { userFinder.findByEmailAndStatus(email, UserStatus.ACTIVE) } returns mockUserInfo
        every { userPasswordManager.verifyPassword(password, userId) } returns Unit
        every { userStateProcessor.updateLastLogin(userId) } returns updatedUserInfo

        // when
        val result = userService.verifyCredentials(email, password)

        // then
        assertThat(result).isEqualTo(updatedUserInfo)
        verify { userFinder.findByEmailAndStatus(email, UserStatus.ACTIVE) }
        verify { userPasswordManager.verifyPassword(password, userId) }
        verify { userStateProcessor.updateLastLogin(userId) }
    }

    @Test
    fun `should register new user`() {
        // given
        val request = UserRegisterParams(
            email = "new@example.com",
            password = "password123",
            name = "New User",
        )
        val userId = 1L
        val initialUserInfo = createMockUserInfo(userId, request.email)

        every { userStateProcessor.createUser(request.email, "", request.name) } returns initialUserInfo
        every { userPasswordManager.changePassword(userId, request.password) } returns initialUserInfo
        every { userStateProcessor.activate(userId) } returns true

        // when
        val result = userService.register(request)

        // then
        assertThat(result).isEqualTo(initialUserInfo)
        verify { userStateProcessor.createUser(request.email, "", request.name) }
        verify { userPasswordManager.changePassword(userId, request.password) }
        verify { userStateProcessor.activate(userId) }
    }

    @Test
    fun `should change password`() {
        // given
        val email = "user@example.com"
        val currentPassword = "oldPassword"
        val newPassword = "newPassword"
        val userId = 1L
        val userInfo = createMockUserInfo(userId, email)
        val updatedUserInfo = userInfo.copy(password = "hashed_new_password")

        every { userFinder.findByEmail(email) } returns userInfo
        every { userPasswordManager.verifyPassword(currentPassword, userId) } returns Unit
        every { userPasswordManager.changePassword(userId, newPassword) } returns updatedUserInfo

        // when
        val result = userService.changePassword(email, currentPassword, newPassword)

        // then
        assertThat(result).isEqualTo(updatedUserInfo)
        verify { userFinder.findByEmail(email) }
        verify { userPasswordManager.verifyPassword(currentPassword, userId) }
        verify { userPasswordManager.changePassword(userId, newPassword) }
    }

    @Test
    fun `should request password reset`() {
        // given
        val email = "user@example.com"
        val userId = 1L
        val userInfo = createMockUserInfo(userId, email)

        every { userFinder.findByEmail(email) } returns userInfo
        every { passwordResetManager.requestPasswordReset(userId, email) } returns true

        // when
        val result = userService.requestPasswordReset(email)

        // then
        assertThat(result).isTrue()
        verify { userFinder.findByEmail(email) }
        verify { passwordResetManager.requestPasswordReset(userId, email) }
    }

    @Test
    fun `should reset password with valid token`() {
        // given
        val token = "valid-reset-token"
        val newPassword = "newSecurePassword"
        val userId = 1L
        val validResult = PasswordResetVerifyResult(true, userId)
        val userInfo = createMockUserInfo(userId, "user@example.com")

        every { passwordResetManager.verifyPasswordResetToken(token) } returns validResult
        every { userFinder.findById(userId) } returns userInfo
        every { userPasswordManager.changePassword(userId, newPassword) } returns userInfo

        // when
        val result = userService.resetPassword(token, newPassword)

        // then
        assertThat(result).isTrue()
        verify { passwordResetManager.verifyPasswordResetToken(token) }
        verify { userFinder.findById(userId) }
        verify { userPasswordManager.changePassword(userId, newPassword) }
    }

    @Test
    fun `should activate user`() {
        // given
        val userId = 1L

        every { userStateProcessor.activate(userId) } returns true

        // when
        val result = userService.activateUser(userId)

        // then
        assertThat(result).isTrue()
        verify { userStateProcessor.activate(userId) }
    }

    @Test
    fun `should inactivate user`() {
        // given
        val userId = 1L

        every { userStateProcessor.inactivate(userId) } returns true

        // when
        val result = userService.inactivateUser(userId)

        // then
        assertThat(result).isTrue()
        verify { userStateProcessor.inactivate(userId) }
    }

    @Test
    fun `should lock user`() {
        // given
        val userId = 1L

        every { userStateProcessor.lock(userId) } returns true

        // when
        val result = userService.lockUser(userId)

        // then
        assertThat(result).isTrue()
        verify { userStateProcessor.lock(userId) }
    }

    @Test
    fun `should unlock user`() {
        // given
        val userId = 1L

        every { userStateProcessor.unlock(userId) } returns true

        // when
        val result = userService.unlockUser(userId)

        // then
        assertThat(result).isTrue()
        verify { userStateProcessor.unlock(userId) }
    }

    @Test
    fun `should delete account and invalidate tokens`() {
        // given
        val userId = 1L
        val password = "userPassword"
        val reason = "Leaving the platform"

        every { userPasswordManager.verifyPassword(password, userId) } returns Unit
        every { userStateProcessor.deleteAccount(userId) } returns true
        every { tokenManager.invalidateAllTokens(userId) } returns Unit

        // when
        val result = userService.deleteAccount(userId, password, reason)

        // then
        assertThat(result).isTrue()
        verify { userPasswordManager.verifyPassword(password, userId) }
        verify { userStateProcessor.deleteAccount(userId) }
        verify { tokenManager.invalidateAllTokens(userId) }
    }

    @Test
    fun `should hard delete user`() {
        // given
        val userId = 1L

        every { userStateProcessor.hardDelete(userId) } returns true

        // when
        val result = userService.hardDeleteUser(userId)

        // then
        assertThat(result).isTrue()
        verify { userStateProcessor.hardDelete(userId) }
    }

    @Test
    fun `should find deleted users`() {
        // given
        val page = 0
        val size = 10
        val user1 = createMockUserInfo(1L, "user1@example.com", UserStatus.DELETED)
        val user2 = createMockUserInfo(2L, "user2@example.com", UserStatus.DELETED)
        val mockPage = PageImpl(listOf(user1, user2))

        every { userFinder.findByStatus(UserStatus.DELETED, page, size) } returns mockPage

        // when
        val result = userService.findDeletedUsers(page, size)

        // then
        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.content[0].id).isEqualTo(1L)
        assertThat(result.content[1].id).isEqualTo(2L)
        verify { userFinder.findByStatus(UserStatus.DELETED, page, size) }
    }

    // Helper methods
    private fun createMockUserInfo(
        id: Long,
        email: String,
        status: UserStatus = UserStatus.ACTIVE,
    ): UserInfo {
        return UserInfo(
            id = id,
            email = email,
            password = "hashed_password",
            name = "Test User",
            status = status,
            role = io.dodn.springboot.storage.db.core.user.UserRole.USER,
            lastLoginAt = LocalDateTime.now().minusDays(1),
            createdAt = LocalDateTime.now().minusDays(10),
            updatedAt = LocalDateTime.now().minusDays(2),
        )
    }
}
