package io.dodn.springboot.core.api.aspect

import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode
import io.dodn.springboot.core.domain.worry.WorryStorage
import io.dodn.springboot.core.support.auth.GominUserDetails
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserRole
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class WorryAccessAspectTest {

    @Mock
    private lateinit var worryStorage: WorryStorage

    @Mock
    private lateinit var joinPoint: ProceedingJoinPoint

    @InjectMocks
    private lateinit var worryAccessAspect: WorryAccessAspect

    private fun createGominUserDetails(id: Long, email: String): GominUserDetails {
        return GominUserDetails(
            id = id,
            email = email,
            encodedPassword = "encoded",
            name = "Test User",
            status = UserStatus.ACTIVE,
            role = UserRole.USER,
            lastLoginAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }

    private fun setupSecurityContext(userId: Long) {
        val userDetails = createGominUserDetails(userId, "test$userId@example.com")
        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities,
        )
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun createWorry(id: Long, userId: Long): Worry {
        return Worry(
            id = id,
            userId = userId,
            mode = WorryMode.LETTER,
            emotion = "슬픔",
            category = "일상",
            content = "Test Content",
            lastStepOrder = 0,
            steps = emptyList(),
            options = emptyList(),
        )
    }

    private fun setupJoinPoint(worryId: Long): ProceedingJoinPoint {
        val mockMethodSignature = mock(org.aspectj.lang.reflect.MethodSignature::class.java)
        `when`(joinPoint.signature).thenReturn(mockMethodSignature)
        `when`(mockMethodSignature.parameterNames).thenReturn(arrayOf("worryId"))
        `when`(joinPoint.args).thenReturn(arrayOf(worryId))
        `when`(joinPoint.proceed()).thenReturn("테스트 결과")
        return joinPoint
    }

    @BeforeEach
    fun setUp() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should allow access to worry for owner`() {
        // Given
        val worryId = 1L
        val userId = 1L
        val worry = createWorry(worryId, userId)

        setupSecurityContext(userId)
        setupJoinPoint(worryId)
        `when`(worryStorage.getWorry(worryId)).thenReturn(worry)

        // When
        val checkAccess = CheckWorryAccess(permission = "VIEW")
        val result = worryAccessAspect.checkAccess(joinPoint, checkAccess)

        // Then
        assertEquals("테스트 결과", result)
        verify(joinPoint, times(1)).proceed()
    }

    @Test
    fun `should deny access to worry for non-owner`() {
        // Given
        val worryId = 1L
        val ownerId = 2L
        val userId = 1L
        val worry = createWorry(worryId, ownerId)

        setupSecurityContext(userId)
        setupJoinPoint(worryId)
        `when`(worryStorage.getWorry(worryId)).thenReturn(worry)

        // When & Then
        val checkAccess = CheckWorryAccess(permission = "VIEW")
        val exception = assertThrows(CoreException::class.java) {
            worryAccessAspect.checkAccess(joinPoint, checkAccess)
        }

        assertEquals(ErrorType.WORRY_PERMISSION_DENIED, exception.errorType)
        verify(joinPoint, never()).proceed()
        joinPoint.proceed()
    }

    @Test
    fun `should allow view access to worry for owner`() {
        // Given
        val worryId = 1L
        val userId = 1L
        val worry = createWorry(worryId, userId)

        setupSecurityContext(userId)
        setupJoinPoint(worryId)
        `when`(worryStorage.getWorry(worryId)).thenReturn(worry)

        // When & Then
        val checkAccess = CheckWorryAccess(permission = "VIEW")
        val result = worryAccessAspect.checkAccess(joinPoint, checkAccess)

        assertEquals("테스트 결과", result)
    }

    @Test
    fun `should allow edit access to worry for owner`() {
        // Given
        val worryId = 1L
        val userId = 1L
        val worry = createWorry(worryId, userId)

        setupSecurityContext(userId)
        setupJoinPoint(worryId)
        `when`(worryStorage.getWorry(worryId)).thenReturn(worry)

        // When & Then
        val checkAccess = CheckWorryAccess(permission = "EDIT")
        val result = worryAccessAspect.checkAccess(joinPoint, checkAccess)

        assertEquals("테스트 결과", result)
    }

    @Test
    fun `should allow delete access to worry for owner`() {
        // Given
        val worryId = 1L
        val userId = 1L
        val worry = createWorry(worryId, userId)

        setupSecurityContext(userId)
        setupJoinPoint(worryId)
        `when`(worryStorage.getWorry(worryId)).thenReturn(worry)

        // When & Then
        val checkAccess = CheckWorryAccess(permission = "DELETE")
        val result = worryAccessAspect.checkAccess(joinPoint, checkAccess)

        assertEquals("테스트 결과", result)
    }

    @Test
    fun `should deny any permission to worry for non-owner`() {
        // Given
        val worryId = 1L
        val ownerId = 1L
        val userId = 2L // 다른 사용자
        val worry = createWorry(worryId, ownerId)

        setupSecurityContext(userId)
        setupJoinPoint(worryId)
        joinPoint.proceed()
        `when`(worryStorage.getWorry(worryId)).thenReturn(worry)

        // When & Then
        val permissions = listOf("VIEW", "EDIT", "DELETE")

        permissions.forEach { permission ->
            val checkAccess = CheckWorryAccess(permission = permission)
            val exception = assertThrows(CoreException::class.java) {
                worryAccessAspect.checkAccess(joinPoint, checkAccess)
            }
            assertEquals(ErrorType.WORRY_PERMISSION_DENIED, exception.errorType)
        }
    }
}
