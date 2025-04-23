package io.dodn.springboot.core.domain.user

import io.dodn.springboot.UnitTest
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.orm.ObjectOptimisticLockingFailureException
import java.time.LocalDateTime
import java.util.Optional

class UserStateProcessorTest : UnitTest() {
    private lateinit var userRepository: UserRepository
    private lateinit var userStateProcessor: UserStateProcessor

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        userStateProcessor = UserStateProcessor(userRepository)
    }

    @Test
    fun `should create user when email doesn't exist`() {
        // given
        val email = "new@example.com"
        val name = "New User"
        val userSlot = slot<UserEntity>()

        every { userRepository.existsByEmail(email) } returns false
        every { userRepository.save(capture(userSlot)) } answers {
            val savedUser = userSlot.captured
            // Simulate ID generation
            val field = savedUser.javaClass.superclass.getDeclaredField("id")
            field.isAccessible = true
            field.set(savedUser, 1L)

            // Add timestamps
            val createdAtField = savedUser.javaClass.superclass.getDeclaredField("createdAt")
            createdAtField.isAccessible = true
            createdAtField.set(savedUser, LocalDateTime.now())

            val updatedAtField = savedUser.javaClass.superclass.getDeclaredField("updatedAt")
            updatedAtField.isAccessible = true
            updatedAtField.set(savedUser, LocalDateTime.now())

            savedUser
        }

        // when
        val result = userStateProcessor.createUser(email, "password", name)

        // then
        assertThat(result.id).isEqualTo(1L)
        assertThat(result.email).isEqualTo(email)
        assertThat(result.name).isEqualTo(name)
        assertThat(result.status).isEqualTo(UserStatus.PENDING_VERIFICATION)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating user with existing email`() {
        // given
        val email = "existing@example.com"

        every { userRepository.existsByEmail(email) } returns true

        // when & then
        assertThrows<CoreException> {
            userStateProcessor.createUser(email, "password", "Existing User")
        }
    }

    @Test
    fun `should activate user when not already active`() {
        // given
        val userId = 1L
        val user = createMockUserEntity(userId, "user@example.com", UserStatus.PENDING_VERIFICATION)

        every { userRepository.findByIdWithOptimisticLock(any()) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { user.apply { status = UserStatus.ACTIVE } }
        val result = userStateProcessor.activate(userId)

        // then
//        assertThat(result).isTrue()
        assertThat(user.status).isEqualTo(UserStatus.ACTIVE)
        verify { userRepository.save(user) }
    }

    @Test
    fun `should throw exception when activating already active user`() {
        // given
        val userId = 1L
        val user = createMockUserEntity(userId, "user@example.com", UserStatus.ACTIVE)

        every { userRepository.findByIdWithOptimisticLock(userId) } returns Optional.of(user)

        // when & then
        assertThrows<CoreException> {
            userStateProcessor.activate(userId)
        }
    }

    @Test
    fun `should throw exception when activating non-existent user`() {
        // given
        val userId = 1L

        every { userRepository.findByIdWithOptimisticLock(userId) } returns Optional.empty()

        // when & then
        assertThrows<CoreException> {
            userStateProcessor.activate(userId)
        }
    }

    @Test
    fun `should throw exception on concurrent modification when activating`() {
        // given
        val userId = 1L

        every { userRepository.findByIdWithOptimisticLock(userId) } throws ObjectOptimisticLockingFailureException("UserEntity", userId)

        // when & then
        assertThrows<CoreException> {
            userStateProcessor.activate(userId)
        }
    }

    @Test
    fun `should inactivate user when not already inactive`() {
        // given
        val userId = 1L
        val user = createMockUserEntity(userId, "user@example.com", UserStatus.ACTIVE)

        every { userRepository.findByIdWithOptimisticLock(userId) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { user.apply { status = UserStatus.INACTIVE } }

        // when
        val result = userStateProcessor.inactivate(userId)

        // then
        assertThat(result).isTrue()
        assertThat(user.status).isEqualTo(UserStatus.INACTIVE)
        verify { userRepository.save(user) }
    }

    @Test
    fun `should throw exception when inactivating already inactive user`() {
        // given
        val userId = 1L
        val user = createMockUserEntity(userId, "user@example.com", UserStatus.INACTIVE)

        every { userRepository.findByIdWithOptimisticLock(userId) } returns Optional.of(user)

        // when & then
        assertThrows<CoreException> {
            userStateProcessor.inactivate(userId)
        }
    }

    @Test
    fun `should lock user when not already locked`() {
        // given
        val userId = 1L
        val user = createMockUserEntity(userId, "user@example.com", UserStatus.ACTIVE)

        every { userRepository.findByIdWithOptimisticLock(userId) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { user.apply { status = UserStatus.LOCKED } }

        // when
        val result = userStateProcessor.lock(userId)

        // then
        assertThat(result).isTrue()
        assertThat(user.status).isEqualTo(UserStatus.LOCKED)
        verify { userRepository.save(user) }
    }

    @Test
    fun `should throw exception when locking already locked user`() {
        // given
        val userId = 1L
        val user = createMockUserEntity(userId, "user@example.com", UserStatus.LOCKED)

        every { userRepository.findByIdWithOptimisticLock(userId) } returns Optional.of(user)

        // when & then
        assertThrows<CoreException> {
            userStateProcessor.lock(userId)
        }
    }

    @Test
    fun `should unlock user when locked`() {
        // given
        val userId = 1L
        val user = createMockUserEntity(userId, "user@example.com", UserStatus.LOCKED)

        every { userRepository.findByIdWithOptimisticLock(userId) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { user.apply { status = UserStatus.ACTIVE } }

        // when
        val result = userStateProcessor.unlock(userId)

        // then
        assertThat(result).isTrue()
        assertThat(user.status).isEqualTo(UserStatus.ACTIVE)
        verify { userRepository.save(user) }
    }

    @Test
    fun `should throw exception when unlocking user that is not locked`() {
        // given
        val userId = 1L
        val user = createMockUserEntity(userId, "user@example.com", UserStatus.ACTIVE)

        every { userRepository.findByIdWithOptimisticLock(userId) } returns Optional.of(user)

        // when & then
        assertThrows<CoreException> {
            userStateProcessor.unlock(userId)
        }
    }

    @Test
    fun `should delete account`() {
        // given
        val userId = 1L
        val user = createMockUserEntity(userId, "user@example.com", UserStatus.ACTIVE)

        every { userRepository.findByIdWithOptimisticLock(userId) } returns Optional.of(user)
        every { userRepository.save(any()) } returns user.apply { status = UserStatus.DELETED }

        // when
        val result = userStateProcessor.deleteAccount(userId)

        // then
        assertThat(result).isTrue()
        assertThat(user.status).isEqualTo(UserStatus.DELETED)
        verify { userRepository.save(user) }
    }

    @Test
    fun `should hard delete user`() {
        // given
        val userId = 1L

        // when
        val result = userStateProcessor.hardDelete(userId)

        // then
        assertThat(result).isTrue()
        verify { userRepository.deleteById(userId) }
    }

    @Test
    fun `should update last login time`() {
        // given
        val userId = 1L
        val user = createMockUserEntity(userId, "user@example.com", UserStatus.ACTIVE)
        val beforeUpdate = LocalDateTime.now().minusDays(1)
        user.lastLoginAt = beforeUpdate

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { userRepository.save(any()) } answers {
            // Update the lastLoginAt field to simulate save
            user.lastLoginAt = LocalDateTime.now()
            user
        }

        // when
        val result = userStateProcessor.updateLastLogin(userId)

        // then
        assertThat(result.id).isEqualTo(userId)
        assertThat(result.lastLoginAt).isNotEqualTo(beforeUpdate)
        verify { userRepository.save(user) }
    }

    // Helper methods
    private fun createMockUserEntity(
        id: Long,
        email: String,
        status: UserStatus,
        name: String? = "Test User",
    ): UserEntity {
        val user = UserEntity(
            email = email,
            password = "hashed_password",
            name = name,
            status = status,
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
