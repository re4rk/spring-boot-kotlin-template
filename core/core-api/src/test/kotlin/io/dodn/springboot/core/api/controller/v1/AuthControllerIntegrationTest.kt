package io.dodn.springboot.core.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import io.dodn.springboot.CoreApiApplication
import io.dodn.springboot.core.api.auth.RefreshTokenRequest
import io.dodn.springboot.core.domain.user.UserRegisterParams
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserRole
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [CoreApiApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val testEmail = "integration-test@example.com"
    private val testPassword = "Test@Password123"
    private val testName = "Integration Test User"

    private var accessToken: String? = null
    private var refreshToken: String? = null

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    @Transactional
    fun `register should create a new user and return success`() {
        // given
        val registerParams = UserRegisterParams(
            email = testEmail,
            password = testPassword,
            name = testName,
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerParams)),
        )
            .andExpect(status().isOk)
            // print response
            .andDo { println("Response: ${it.response.contentAsString}") }
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.user.email").value(testEmail))
            .andExpect(jsonPath("$.data.user.name").value(testName))
            .andExpect(jsonPath("$.data.user.status").value("ACTIVE"))

        // verify user was created in database
        val user = userRepository.findByEmail(testEmail).orElseThrow()
        assert(user.email == testEmail)
        assert(user.name == testName)
        assert(user.status == UserStatus.ACTIVE)
    }

    @Test
    @Transactional
    fun `login should authenticate user and return tokens`() {
        // given
        createTestUser()

        val loginRequest = mapOf(
            "email" to testEmail,
            "password" to testPassword,
        )

        // when & then
        val result = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.user.email").value(testEmail))
            .andExpect(jsonPath("$.data.user.name").value(testName))
            .andReturn()

        // Save tokens for other tests
        val responseJson = result.response.contentAsString
        val responseMap = objectMapper.readValue(responseJson, Map::class.java)
        val data = responseMap["data"] as Map<*, *>
        accessToken = data["accessToken"] as String
        refreshToken = data["refreshToken"] as String
    }

    @Test
    @Transactional
    fun `refresh token should return new tokens`() {
        // given
        createTestUserAndLogin()

        val refreshRequest = RefreshTokenRequest(refreshToken!!)

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.user.email").value(testEmail))
    }

    @Test
    @Transactional
    fun `me endpoint should return current user info`() {
        // given
        createTestUserAndLogin()

        // when & then
        mockMvc.perform(
            get("/api/v1/auth/me")
                .header("Authorization", "Bearer $accessToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.email").value(testEmail))
            .andExpect(jsonPath("$.data.name").value(testName))
    }

    @Test
    @Transactional
    fun `logout should invalidate user refresh tokens`() {
        // given
        createTestUserAndLogin()

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/logout")
                .header("Authorization", "Bearer $accessToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))

        // Atter logout, the refresh token should be invalid
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RefreshTokenRequest(refreshToken!!))),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.result").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("E401"))
    }

    @Test
    @Transactional
    fun `change password should update user password`() {
        // given
        createTestUserAndLogin()

        val changePasswordRequest = mapOf(
            "oldPassword" to testPassword,
            "newPassword" to "NewPassword@123",
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/change-password")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data").value(true))

        // Verify we can login with new password
        val loginRequest = mapOf(
            "email" to testEmail,
            "password" to "NewPassword@123",
        )

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)),
        ).apply {
            // println("Response: ${this.andReturn().response.contentAsString}")
            println("Response: ${this.andReturn().response.contentAsString}")
        }
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
    }

    @Test
    @Transactional
    fun `register with existing email should return error`() {
        // given
        createTestUser()

        val registerParams = UserRegisterParams(
            email = testEmail, // Same email as existing user
            password = "AnotherPassword123",
            name = "Another User",
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerParams)),
        )
            .andExpect(status().isConflict) // 409 Conflict
            .andExpect(jsonPath("$.result").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("E409"))
    }

    // Helper methods

    private fun createTestUser() {
        val user = UserEntity(
            email = testEmail,
            password = passwordEncoder.encode(testPassword),
            name = testName,
            status = UserStatus.ACTIVE,
            role = UserRole.USER,
        )
        userRepository.save(user)
    }

    private fun createTestUserAndLogin() {
        createTestUser()

        val loginRequest = mapOf(
            "email" to testEmail,
            "password" to testPassword,
        )

        val result = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)),
        )
            .andReturn()

        val responseJson = result.response.contentAsString
        val responseMap = objectMapper.readValue(responseJson, Map::class.java)
        val data = responseMap["data"] as Map<*, *>
        accessToken = data["accessToken"] as String
        refreshToken = data["refreshToken"] as String
    }
}
