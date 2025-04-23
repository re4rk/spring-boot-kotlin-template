package io.dodn.springboot.core.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import io.dodn.springboot.CoreApiApplication
import io.dodn.springboot.core.api.controller.v1.request.CreateLetterWorryRequest
import io.dodn.springboot.core.api.controller.v1.request.OptionReqeust
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserRole
import io.dodn.springboot.storage.db.core.user.UserStatus
import io.dodn.springboot.storage.db.core.worry.WorryRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest(
    classes = [CoreApiApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("local")
class WorryControllerStreamingTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var worryRepository: WorryRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val testEmail = "worry-stream-test@example.com"
    private val testPassword = "Test@Password123"
    private val testName = "Worry Stream Test User"
    private var testUserId: Long = 0
    private var accessToken: String? = null

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        createTestUserAndLogin()
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    fun `should create letter worry and get streaming feedback`() {
        // given
        val worryId = createTestWorry()

        println("\n=== 스트리밍 응답 테스트 시작 (HttpURLConnection 사용) ===")

        // 실제 URL 연결로 스트리밍 응답 테스트
        val url = URL("http://localhost:$port/api/v1/worries/$worryId/streaming-feedback")
        val connection = url.openConnection() as HttpURLConnection

        // 요청 설정
        connection.requestMethod = "POST"
        connection.setRequestProperty("Accept", "text/event-stream")
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.doInput = true

        // 연결 시작
        connection.connect()

        // 응답 상태 확인
        val responseCode = connection.responseCode
        println("응답 코드: $responseCode")
        assertTrue(responseCode in 200..299, "응답이 성공이어야 합니다 (200-299). 실제: $responseCode")

        // 입력 스트림 읽기
        val reader = BufferedReader(InputStreamReader(connection.inputStream))

        // 별도 스레드에서 스트리밍 응답 읽기
        val latch = CountDownLatch(1)
        Thread {
            try {
                println("스트리밍 응답 읽기 시작...")
                var line: String?
                var isFirstChunk = true
                var currentEvent = ""
                var isComplete = false

                while (reader.readLine().also { line = it } != null && !isComplete) {
                    if (line.isNullOrBlank()) continue

                    when {
                        line!!.startsWith("event:") -> {
                            currentEvent = line!!.substring(6).trim()
                            println("\n이벤트 타입: $currentEvent")
                        }

                        line!!.startsWith("data:") -> {
                            val data = line!!.substring(5)

                            if (currentEvent == "chunk") {
                                // 청크 이벤트는 연속해서 출력
                                if (isFirstChunk) {
                                    print("\n응답 내용: ")
                                    isFirstChunk = false
                                }
                                print(data)
                                System.out.flush()
                            } else {
                                println("\n[$currentEvent] $data")

                                // complete 이벤트를 받으면 스트림 종료
                                if (currentEvent == "complete") {
                                    isComplete = true
                                    println("\n=== 스트리밍 응답 완료 ===")
                                }
                            }
                        }
                    }
                }

                if (!isComplete) {
                    println("\n=== 스트림이 예상치 못하게 종료됨 ===")
                }
            } catch (e: Exception) {
                println("스트리밍 읽기 중 오류 발생: ${e.message}")
                e.printStackTrace()
            } finally {
                latch.countDown()
                try {
                    reader.close()
                    connection.disconnect()
                } catch (e: Exception) {
                    println("리소스 정리 중 오류: ${e.message}")
                }
            }
        }.start()

        // 최대 2분까지 대기 (실제 응답 시간에 따라 조정)
        val timeout = 120L
        if (!latch.await(timeout, TimeUnit.SECONDS)) {
            println("\n=== 타임아웃: ${timeout}초가 지났습니다 ===")
        }

        println("\n=== 스트리밍 응답 테스트 종료 ===")
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

        // HttpURLConnection을 사용한 로그인
        val url = URL("http://localhost:$port/api/v1/auth/login")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        // 로그인 요청 본문 작성
        val loginRequest = mapOf(
            "email" to testEmail,
            "password" to testPassword,
        )

        val requestBody = objectMapper.writeValueAsString(loginRequest)

        // 요청 전송
        DataOutputStream(connection.outputStream).use { it.writeBytes(requestBody) }

        // 응답 상태 확인
        val responseCode = connection.responseCode
        println("로그인 응답 코드: $responseCode")

        if (responseCode !in 200..299) {
            throw RuntimeException("로그인 실패: HTTP $responseCode")
        }

        // 응답 내용 읽기
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }

        // 응답에서 토큰 추출
        val responseMap = objectMapper.readValue(response.toString(), Map::class.java)
        val data = responseMap["data"] as Map<*, *>
        accessToken = data["accessToken"] as String

        reader.close()
        connection.disconnect()

        println("로그인 성공: 액세스 토큰 취득 완료")
    }

    private fun createTestWorry(): Long {
        // 테스트 고민 생성 (HttpURLConnection 사용)
        val url = URL("http://localhost:$port/api/v1/worries/letter")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.doOutput = true

        // 고민 요청 본문 작성
        val letterWorryRequest = CreateLetterWorryRequest(
            userId = testUserId,
            emotion = "Anxiety",
            content = "I'm worried about my upcoming exam",
            category = "Study",
            options = listOf(
                OptionReqeust("Study more"),
                OptionReqeust("Take a break"),
            ),
        )

        val requestBody = objectMapper.writeValueAsString(letterWorryRequest)

        // 요청 전송
        DataOutputStream(connection.outputStream).use { it.writeBytes(requestBody) }

        // 응답 상태 확인
        val responseCode = connection.responseCode
        println("고민 생성 응답 코드: $responseCode")

        if (responseCode !in 200..299) {
            throw RuntimeException("고민 생성 실패: HTTP $responseCode")
        }

        // 응답 내용 읽기
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }

        // 응답에서 고민 ID 추출
        val responseMap = objectMapper.readValue(response.toString(), Map::class.java)
        val data = responseMap["data"] as Map<*, *>
        val worryId = (data["worryId"] as Int).toLong()

        reader.close()
        connection.disconnect()

        println("고민 생성 성공: ID = $worryId")
        return worryId
    }
}
