package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.auth.AuthFacade
import io.dodn.springboot.core.api.auth.AuthResponse
import io.dodn.springboot.core.api.auth.RefreshTokenRequest
import io.dodn.springboot.core.api.auth.RegisterResponse
import io.dodn.springboot.core.api.controller.v1.request.UserLoginRequest
import io.dodn.springboot.core.domain.user.UserInfo
import io.dodn.springboot.core.domain.user.UserRegisterParams
import io.dodn.springboot.core.support.auth.GominUserDetails
import io.dodn.springboot.storage.db.core.user.UserRole
import io.dodn.springboot.storage.db.core.user.UserStatus
import io.dodn.springboot.test.api.RestDocsTest
import io.dodn.springboot.test.api.RestDocsUtils.requestPreprocessor
import io.dodn.springboot.test.api.RestDocsUtils.responsePreprocessor
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import java.time.LocalDateTime

class AuthControllerTest : RestDocsTest() {
    private lateinit var authFacade: AuthFacade
    private lateinit var controller: AuthController

    private lateinit var mockUserInfo: UserInfo
    private lateinit var mockAuthResponse: AuthResponse
    private lateinit var mockRegisterRequest: RegisterResponse

    @BeforeEach
    fun setUp() {
        // 서비스 모킹
        authFacade = mockk()
        controller = AuthController(authFacade)
        mockMvc = mockController(controller)

        // Mock 데이터 설정
        mockUserInfo = UserInfo(
            id = 1L,
            email = "test@example.com",
            name = "Test User",
            status = UserStatus.ACTIVE,
            role = UserRole.USER,
            lastLoginAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            password = "hashed-password",
        )

        mockAuthResponse = AuthResponse(
            accessToken = "mock-access-token",
            refreshToken = "mock-refresh-token",
            user = mockUserInfo,
        )
    }

    @Test
    fun registerTest() {
        // Given
        val registerRequest = UserRegisterParams(
            email = "test@example.com",
            password = "password123",
            name = "Test User",
        )

        every { authFacade.register(registerRequest) } returns mockRegisterRequest

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(registerRequest)
            .post("/api/v1/auth/register")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "auth-register",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestFields(
                        fieldWithPath("email").type(JsonFieldType.STRING).description("사용자 이메일"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("사용자 비밀번호"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("사용자 이름").optional(),
                    ),
                    responseFields(
                        fieldWithPath("result").type(JsonFieldType.STRING).description("결과 타입"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                        fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                        fieldWithPath("data.user.id").type(JsonFieldType.NUMBER).description("사용자 ID"),
                        fieldWithPath("data.user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                        fieldWithPath("data.user.name").type(JsonFieldType.STRING).description("사용자 이름").optional(),
                        fieldWithPath("data.user.status").type(JsonFieldType.STRING).description("사용자 상태"),
                        fieldWithPath("data.user.role").type(JsonFieldType.STRING).description("사용자 역할"),
                        fieldWithPath("data.user.lastLoginAt").type(JsonFieldType.STRING).description("마지막 로그인 시간")
                            .optional(),
                        fieldWithPath("data.user.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                        fieldWithPath("data.user.updatedAt").type(JsonFieldType.STRING).description("수정 시간"),
                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                    ),
                ),
            )
    }

    @Test
    fun loginTest() {
        // Given
        val loginRequest = UserLoginRequest(
            email = "test@example.com",
            password = "password123",
        )

        every { authFacade.login(loginRequest.email, loginRequest.password) } returns mockAuthResponse

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .post("/api/v1/auth/login")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "auth-login",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestFields(
                        fieldWithPath("email").type(JsonFieldType.STRING).description("사용자 이메일"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("사용자 비밀번호"),
                    ),
                    responseFields(
                        fieldWithPath("result").type(JsonFieldType.STRING).description("결과 타입"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                        fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                        fieldWithPath("data.user.id").type(JsonFieldType.NUMBER).description("사용자 ID"),
                        fieldWithPath("data.user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                        fieldWithPath("data.user.name").type(JsonFieldType.STRING).description("사용자 이름").optional(),
                        fieldWithPath("data.user.status").type(JsonFieldType.STRING).description("사용자 상태"),
                        fieldWithPath("data.user.role").type(JsonFieldType.STRING).description("사용자 역할"),
                        fieldWithPath("data.user.lastLoginAt").type(JsonFieldType.STRING).description("마지막 로그인 시간")
                            .optional(),
                        fieldWithPath("data.user.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                        fieldWithPath("data.user.updatedAt").type(JsonFieldType.STRING).description("수정 시간"),
                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                    ),
                ),
            )
    }

    @Test
    fun refreshTokenTest() {
        // Given
        val refreshRequest = RefreshTokenRequest(
            refreshToken = "old-refresh-token",
        )

        every { authFacade.refreshToken(refreshRequest) } returns mockAuthResponse

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(refreshRequest)
            .post("/api/v1/auth/refresh")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "auth-refresh",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestFields(
                        fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                    ),
                    responseFields(
                        fieldWithPath("result").type(JsonFieldType.STRING).description("결과 타입"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("새 액세스 토큰"),
                        fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("새 리프레시 토큰"),
                        fieldWithPath("data.user.id").type(JsonFieldType.NUMBER).description("사용자 ID"),
                        fieldWithPath("data.user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                        fieldWithPath("data.user.name").type(JsonFieldType.STRING).description("사용자 이름").optional(),
                        fieldWithPath("data.user.status").type(JsonFieldType.STRING).description("사용자 상태"),
                        fieldWithPath("data.user.role").type(JsonFieldType.STRING).description("사용자 역할"),
                        fieldWithPath("data.user.lastLoginAt").type(JsonFieldType.STRING).description("마지막 로그인 시간")
                            .optional(),
                        fieldWithPath("data.user.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                        fieldWithPath("data.user.updatedAt").type(JsonFieldType.STRING).description("수정 시간"),
                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                    ),
                ),
            )
    }

//    @Test
//    fun logoutTest() {
//        // Given
//        every { authFacade.logout(any<GominUserDetails>(),) } returns Unit
//
//        // When & Then
//        given()
//            .contentType(ContentType.JSON)
//            .post("/api/v1/auth/logout")
//            .then()
//            .status(HttpStatus.OK)
//            .apply(
//                document(
//                    "auth-logout",
//                    requestPreprocessor(),
//                    responsePreprocessor(),
//                    responseFields(
//                        fieldWithPath("result").type(JsonFieldType.STRING).description("결과 타입"),
//                        fieldWithPath("data").type(JsonFieldType.NULL).description("데이터 없음"),
//                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
//                    ),
//                ),
//            )
//    }

    @Test
    fun getCurrentUserTest() {
        // Given
        every { authFacade.getCurrentUser() } returns mockUserInfo

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .get("/api/v1/auth/me")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "auth-me",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    responseFields(
                        fieldWithPath("result").type(JsonFieldType.STRING).description("결과 타입"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("사용자 ID"),
                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                        fieldWithPath("data.name").type(JsonFieldType.STRING).description("사용자 이름").optional(),
                        fieldWithPath("data.status").type(JsonFieldType.STRING).description("사용자 상태"),
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description("사용자 역할"),
                        fieldWithPath("data.lastLoginAt").type(JsonFieldType.STRING).description("마지막 로그인 시간")
                            .optional(),
                        fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                        fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 시간"),
                        fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                    ),
                ),
            )
    }
}
