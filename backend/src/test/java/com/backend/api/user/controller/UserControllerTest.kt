package com.backend.api.user.controller

import com.backend.api.global.JwtTest
import com.backend.api.user.dto.request.UserLoginRequest
import com.backend.api.user.dto.request.UserSignupRequest
import com.backend.config.TestRedisConfig
import com.backend.domain.user.entity.RefreshToken
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.entity.VerificationCode
import com.backend.domain.user.repository.RefreshRedisRepository
import com.backend.domain.user.repository.UserRepository
import com.backend.domain.user.repository.VerificationCodeRepository
import com.backend.global.security.JwtTokenProvider
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime



@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@Import(TestRedisConfig::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    override var userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val refreshRedisRepository: RefreshRedisRepository
) : JwtTest() {



    @Nested
    @DisplayName("회원가입 API")
    inner class T1 {
        @Test
        @DisplayName("정상 작동")
        fun success() {
            // given
            val email = "signup1@naver.com"

            //이메일 인증 완료 상태를 DB에 저장
            verificationCodeRepository.save(
                VerificationCode(
                    email=email,
                    code = "ABC123",
                    verified = true,
                    expiresAt = LocalDateTime.now().plusMinutes(5)
                )
            )

            val request = UserSignupRequest(
                email,
                "test1234",
                "test",
                "signupNick",
                27,
                "https://github.com/signup",
                null
            )

            // when
            val resultActions = mockMvc.perform(
                post("/api/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            val user = userRepository.findByEmail(request.email)

            resultActions
                .andExpect(handler().handlerType(UserController::class.java))
                .andExpect(handler().methodName("signup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.email").value(request.email))
                .andExpect(jsonPath("$.data.nickname").value(request.nickname))
                .andDo(print())
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입")
        fun fail() {
            val existingUser = userRepository.save(
                User(
                    email = "signup2@naver.com",
                    password = "test1234",
                    name = "test",
                    nickname = "signupNick2",
                    age = 27,
                    github = "https://github.com/signup2",
                    role = Role.USER
                )
            )

            val request = UserSignupRequest(
                "signup2@naver.com",
                "test1234",
                "test",
                "signupNick2",
                27,
                "https://github.com/signup2",
                null
            )

            val resultActions = mockMvc.perform(
                post("/api/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(handler().handlerType(UserController::class.java))
                .andExpect(handler().methodName("signup"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("이메일 인증 미완료 상태에서 회원가입 실패")
        fun fail_notVerifiedEmail() {
            val email = "signup3@naver.com"

            //인증코드는 있지만 verified=false 인 상태
            verificationCodeRepository.save(
                VerificationCode(
                    email=email,
                    code = "XYZ999",
                    verified = false,
                    expiresAt = LocalDateTime.now().plusMinutes(5)
                )
            )

            val request = UserSignupRequest(
                email,
                "test1234",
                "test",
                "signupNick3",
                27,
                "https://github.com/signup3",
                null
            )

            val resultActions = mockMvc.perform(
                post("/api/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("이메일 인증이 완료되지 않았습니다."))
                .andDo(print())
        }
    }


    @Nested
    @DisplayName("로그인 API")
    inner class T2 {
        @Test
        @DisplayName("정상 작동 - 토큰 설정 확인")
        fun success() {
            val request = UserLoginRequest(
                mockUser.email,
                "test1234"
            )

            val resultActions = mockMvc.perform(
                post("/api/v1/users/login")
                    .secure(true)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(handler().handlerType(UserController::class.java))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("로그인을 성공했습니다."))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andDo(print())
        }

        @Test
        @DisplayName("이메일 불일치")
        fun fail1() {
            val request = UserLoginRequest(
                "wrong@naver.com",
                "test1234"
            )

            val resultActions = mockMvc.perform(
                post("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(handler().handlerType(UserController::class.java))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("이메일이 존재하지 않습니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("비밀번호 불일치")
        fun fail2() {
            val request = UserLoginRequest(
                mockUser.email,
                "wrong1234"
            )

            val resultActions = mockMvc.perform(
                post("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )



            resultActions
                .andExpect(handler().handlerType(UserController::class.java))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andDo(print())
        }
    }


    @Nested
    @DisplayName("로그아웃")
    inner class T3 {
        @Test
        @DisplayName("정상 작동")
        fun success() {
            val resultActions = mockMvc
                .perform(
                    delete("/api/v1/users/logout")
                )
                .andDo(print())

            resultActions
                .andExpect(handler().handlerType(UserController::class.java))
                .andExpect(handler().methodName("logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("로그아웃이 되었습니다."))
                .andDo(print())
        }
    }


    @Nested
    @DisplayName("토큰 재발급 API")
    inner class T4 {
        @Test
        @DisplayName("토큰 갱신 정상 작동")
        fun success() {
            val refreshToken = jwtTokenProvider.generateRefreshToken(
                mockUser.id,
                mockUser.email,
                mockUser.role
            )


            val refreshEntity = RefreshToken(
                userId = mockUser.id,
                refreshToken = refreshToken,
                expiration = jwtTokenProvider.getRefreshTokenExpireTime() / 1000
            )
            refreshRedisRepository.save(refreshEntity)

            val resultActions = mockMvc
                .perform(
                    post("/api/v1/users/refresh")
                        .cookie(Cookie("refreshToken", refreshToken))
                )
                .andDo(print())

            resultActions
                .andExpect(handler().handlerType(UserController::class.java))
                .andExpect(handler().methodName("refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("새로운 토큰이 발급되었습니다."))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().exists("accessToken"))
                .andDo(print())
        }

        @Test
        @DisplayName("존재하지 않는 refreshToken")
        fun fail1() {
            val resultActions = mockMvc
                .perform( //토큰 없이 요청
                    post("/api/v1/users/refresh")
                )
                .andDo(print())

            resultActions
                .andExpect(handler().handlerType(UserController::class.java))
                .andExpect(handler().methodName("refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Refresh Token을 찾을 수 없습니다."))
                .andDo(print())
        }


        @Test
        @DisplayName("유효하지 않은 refreshToken")
        fun fail2() {
            val wrongRefreshToken = "wrong-access-token"
            val resultActions = mockMvc
                .perform(
                    post("/api/v1/users/refresh")

                        .cookie(Cookie("refreshToken", wrongRefreshToken))
                )
                .andDo(print())

            resultActions
                .andExpect(handler().handlerType(UserController::class.java))
                .andExpect(handler().methodName("refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token입니다."))
                .andDo(print())
        }
    }
}