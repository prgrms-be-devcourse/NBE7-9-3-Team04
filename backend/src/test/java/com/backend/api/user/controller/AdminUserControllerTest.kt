package com.backend.api.user.controller

import com.backend.api.user.dto.request.AdminUserStatusUpdateRequest
import com.backend.api.user.service.EmailService
import com.backend.domain.user.entity.AccountStatus
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@TestInstance(
    TestInstance.Lifecycle.PER_CLASS
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AdminUserControllerTest(

    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
) {
    @MockBean
    lateinit var rq: Rq

    @MockBean
    lateinit var emailService: EmailService

    lateinit var admin: User
    lateinit var user: User

    @BeforeEach
    fun setUp() {
        admin = userRepository.save(
            User(
                email = "admin@test.com",
                password = "admin1234!",
                name = "관리자",
                nickname = "admin",
                age = 30,
                github = "github.com/admin",
                image = null,
                role = Role.ADMIN
            )
        )

        user = userRepository.save(
            User(
                email = "user@test.com",
                password = "user1234!",
                name = "일반유저",
                nickname = "user",
                age = 25,
                github = "github.com/user",
                image = null,
                role = Role.USER
            )
        )

        Mockito.`when`(rq.getUser()).thenReturn(admin)
    }

    @Nested
    @DisplayName("관리자 전체 사용자 조회 API")
    inner class T1 {
        @Test
        @DisplayName("전체 사용자 조회 성공")

        fun success() {
            mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("전체 사용자 조회 성공"))
                .andExpect(jsonPath("$.data.users").isArray())
                .andExpect(jsonPath("$.data.users[*].name", hasItem("일반유저")))
                .andDo(print())
        }

        @Test
        @DisplayName("실패 - 비로그인 상태")

        fun fail1() {
            Mockito.`when`(rq.getUser()).thenReturn(null)

            mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("로그인된 사용자가 없습니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("실패 - 관리자 아닌 경우")
        @Throws(Exception::class)
        fun fail2() {
            Mockito.`when`(rq.getUser()).thenReturn(user)

            mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("관리자 특정 사용자 조회 API")
    inner class T2 {
        @Test
        @DisplayName("특정 사용자 조회 성공")
        fun success() {
            mockMvc.perform(get("/api/v1/admin/users/{userId}", user.id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("특정 사용자 조회 성공"))
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andDo(print())
        }

        @Test
        @DisplayName("실패 - 대상 사용자 없음")
        fun fail1() {
            val notExistId = 9999999L

            mockMvc.perform(get("/api/v1/admin/users/{userId}", notExistId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("관리자 사용자 상태 변경 API")
    inner class T3 {
        @Test
        @DisplayName("사용자 상태 변경 성공 - ACTIVE → BANNED")
        fun success_banned() {
            val request = AdminUserStatusUpdateRequest(
                AccountStatus.BANNED,
                "심각한 약관 위반",
                null
            )

            mockMvc.perform(
                patch("/api/v1/admin/users/{userId}/status", user.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("사용자 상태 변경 성공"))
                .andExpect(jsonPath("$.data.accountStatus").value("BANNED"))
                .andDo(print())
        }

        @Test
        @DisplayName("사용자 상태 변경 성공 - ACTIVE → SUSPENDED")
        fun success_suspended() {
            val request = AdminUserStatusUpdateRequest(
                AccountStatus.SUSPENDED,
                "신고 누적",
                LocalDate.now().plusDays(7)
            )

            mockMvc.perform(
                patch("/api/v1/admin/users/{userId}/status", user.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.accountStatus").value("SUSPENDED"))
                .andDo(print())
        }

        @Test
        @DisplayName("사용자 상태 변경 성공 - SUSPENDED → ACTIVE (정지 해제)")
        fun success_reactivate() {
            // 먼저 정지 상태로 설정
            user.changeStatus(AccountStatus.SUSPENDED)
            userRepository.save(user)

            val request = AdminUserStatusUpdateRequest(
                AccountStatus.ACTIVE,
                null,
                null
            )

            mockMvc.perform(
                patch("/api/v1/admin/users/{userId}/status", user.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"))
                .andDo(print())
        }

        @Test
        @DisplayName("실패 - 일시정지인데 사유 누락")
        fun fail_missing_reason() {
            val request = AdminUserStatusUpdateRequest(
                AccountStatus.SUSPENDED,
                null,
                LocalDate.now().plusDays(3)
            )

            mockMvc.perform(
                patch("/api/v1/admin/users/{userId}/status", user.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("정지 사유가 필요합니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("실패 - 영구정지인데 종료일 지정")
        fun fail_banned_with_endDate() {
            val request = AdminUserStatusUpdateRequest(
                AccountStatus.BANNED,
                "영구정지 테스트",
                LocalDate.now().plusDays(30)
            )

            mockMvc.perform(
                patch("/api/v1/admin/users/{userId}/status", user.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("영구 정지 상태에서는 종료일을 지정할 수 없습니다."))
                .andDo(print())
        }
    }
}