package io.dodn.springboot.core.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import io.dodn.springboot.CoreApiApplication
import io.dodn.springboot.core.api.controller.v1.request.CreateConversationRequest
import io.dodn.springboot.core.api.controller.v1.request.CreateConvoWorryRequest
import io.dodn.springboot.core.api.controller.v1.request.CreateFeedbackRequest
import io.dodn.springboot.core.api.controller.v1.request.CreateLetterWorryRequest
import io.dodn.springboot.core.api.controller.v1.request.OptionReqeust
import io.dodn.springboot.core.api.controller.v1.request.StepRequest
import io.dodn.springboot.core.domain.worry.StepRole
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserRole
import io.dodn.springboot.storage.db.core.user.UserStatus
import io.dodn.springboot.storage.db.core.worry.WorryRepository
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
class WorryControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var worryRepository: WorryRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val testEmail = "worry-test@example.com"
    private val testPassword = "Test@Password123"
    private val testName = "Worry Test User"
    private var testUserId: Long = 0
    private var accessToken: String? = null

    @BeforeEach
    fun setUp() {
        // 기존 데이터 정리
        worryRepository.deleteAll()
        userRepository.deleteAll()

        // 테스트 사용자 생성
        createTestUserAndLogin()
    }

    @AfterEach
    fun tearDown() {
        worryRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @Transactional
    fun `should create letter worry`() {
        // given
        val letterWorryRequest = CreateLetterWorryRequest(
            emotion = "Anxiety",
            content = "I'm worried about my upcoming exam",
            category = "Study",
            options = listOf(
                OptionReqeust("Study more"),
                OptionReqeust("Take a break"),
            ),
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/worries/letter")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(letterWorryRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.worryId").exists())
    }

    @Test
    @Transactional
    fun `should create conversation worry`() {
        // given
        val convoWorryRequest = CreateConvoWorryRequest(
            emotion = "Confusion",
            category = "Career",
            steps = listOf(
                StepRequest(StepRole.USER, "I'm not sure what career path to choose"),
                StepRequest(StepRole.AI, "That's a common concern. What are your interests?"),
                StepRequest(StepRole.USER, "I like technology and helping people"),
            ),
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/worries/convo")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(convoWorryRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.worryId").exists())
    }

    @Test
    @Transactional
    fun `should get worry by id`() {
        // given
        // 먼저 고민 생성
        val worryId = createTestLetterWorry()

        // when & then
        mockMvc.perform(
            get("/api/v1/worries/$worryId")
                .header("Authorization", "Bearer $accessToken"),
        )
            .andExpect(status().isOk)
            .andDo { println("Response: ${it.response.contentAsString}") }
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.worryId").value(worryId))
            .andExpect(jsonPath("$.data.emotion").value("Anxiety"))
            .andExpect(jsonPath("$.data.content").value("I'm worried about my upcoming exam"))
    }

    @Test
    @Transactional
    fun `should create feedback for worry`() {
        // given
        val worryId = createTestLetterWorry()
        val feedbackRequest = CreateFeedbackRequest(
            feedback = "It's normal to feel anxious before exams. Try to break down your study plan into manageable tasks.",
            tone = "Supportive",
            tags = listOf("anxiety", "academic", "stress-management"),
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/worries/$worryId/feedback")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedbackRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.feedback").exists())
//            .andExpect(jsonPath("$.data.tone").value("Supportive"))
//            .andExpect(jsonPath("$.data.tags").isArray())
    }

    @Test
    @Transactional
    fun `should generate summary for worry`() {
        // given
        val worryId = createTestLetterWorry()

        // when & then
        mockMvc.perform(
            get("/api/v1/worries/$worryId/summary")
                .header("Authorization", "Bearer $accessToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.summary").exists())
    }

    @Test
    @Transactional
    fun `should add conversation to existing worry`() {
        // given
        // 먼저 고민 생성
        val worryId = createTestConvoWorry()

        // 대화 요청 생성
        val conversationRequest = CreateConversationRequest(
            conversation = "I've been studying for hours but still feel unprepared",
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/worries/$worryId/conversation")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conversationRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.worryId").value(worryId))
            .andExpect(jsonPath("$.data.steps[${0}].role").value("user"))
            .andExpect(jsonPath("$.data.steps[${0}].content").value("I've been studying for hours but still feel unprepared"))
    }

    // Helper methods
    private fun createTestUser(): UserEntity {
        val user = UserEntity(
            email = testEmail,
            password = passwordEncoder.encode(testPassword),
            name = testName,
            status = UserStatus.ACTIVE,
            role = UserRole.USER,
        )
        val savedUser = userRepository.save(user)
        testUserId = savedUser.id
        return savedUser
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
    }

    private fun createTestLetterWorry(): Long {
        val letterWorryRequest = CreateLetterWorryRequest(
            emotion = "Anxiety",
            content = "I'm worried about my upcoming exam",
            category = "Study",
            options = listOf(
                OptionReqeust("Study more"),
                OptionReqeust("Take a break"),
            ),
        )

        val result = mockMvc.perform(
            post("/api/v1/worries/letter")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(letterWorryRequest)),
        )
            .andReturn()

        val responseJson = result.response.contentAsString
        val responseMap = objectMapper.readValue(responseJson, Map::class.java)
        val data = responseMap["data"] as Map<*, *>
        return (data["worryId"] as Int).toLong()
    }

    private fun createTestConvoWorry(): Long {
        val convoWorryRequest = CreateConvoWorryRequest(
            emotion = "Confusion",
            category = "Career",
            steps = listOf(
                StepRequest(StepRole.USER, "I'm not sure what career path to choose"),
                StepRequest(StepRole.AI, "That's a common concern. What are your interests?"),
                StepRequest(StepRole.USER, "I like technology and helping people"),
            ),
        )

        val result = mockMvc.perform(
            post("/api/v1/worries/convo")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(convoWorryRequest)),
        )
            .andReturn()

        val responseJson = result.response.contentAsString
        val responseMap = objectMapper.readValue(responseJson, Map::class.java)
        val data = responseMap["data"] as Map<*, *>
        return (data["worryId"] as Int).toLong()
    }
}
