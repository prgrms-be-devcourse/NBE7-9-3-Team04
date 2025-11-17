/*
package com.backend.api.user.controller

import com.backend.api.user.dto.response.UserMyPageResponse
import com.backend.api.user.dto.response.UserMyPageResponse.UserModify
import com.backend.api.user.service.UserMyPageService
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.junit.jupiter.api.*
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MyPageControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository) {

    @MockBean
    lateinit var userMyPageService: UserMyPageService

    @MockBean
    lateinit var rq: Rq

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = User(
            email = "user@test.com",
            password = "user1234!",
            name = "일반유저",
            nickname = "user",
            age = 25,
            github = "github.com/user",
            image = null,
            role = Role.USER
        )
        ReflectionTestUtils.setField(user, "id", 1L)
        Mockito.`when`(rq.getUser()).thenReturn(user)
    }

    @Nested
    @DisplayName("사용자 전체 API")
    inner class Test1 {
        @Test
        @DisplayName("로그인 상태")
        fun success() {
            val response = UserMyPageResponse.fromEntity(user)
            Mockito.`when`(userMyPageService.getInformation(user.id)).thenReturn(response)

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/me"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value("user@test.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("일반유저"))
                .andDo(MockMvcResultHandlers.print())
        }

        //비로그인 상태 오류 401 에러
//        @Test
//        @DisplayName("비로그인 상태")
//        fun fail1() {
//            //Mockito.`when`(rq.getUser()).thenReturn(null)
//            userRepository.deleteAll()
//
//            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/me"))
//                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UNAUTHORIZED"))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("로그인된 사용자가 없습니다."))
//                .andDo(MockMvcResultHandlers.print())
//        }
    }

    @Nested
    @DisplayName("개인정보 비밀번호 API")
    inner class Test2 {
        @Test
        @DisplayName("성공 - 비밀번호 일치")
        fun success() {
            val requestBody = mapOf("password" to "user1234!")
            Mockito.`when`(userMyPageService.verifyPassword(user.id, "user1234!")).thenReturn(true)

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/verifyPassword")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody))
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("비밀번호가 확인되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(true))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        fun fail1() {
            val requestBody = mapOf("password" to "wrongPassword")
            Mockito.`when`(userMyPageService.verifyPassword(user.id, "wrongPassword")).thenReturn(false)

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/verifyPassword")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody))
            )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("마이페이지 개인 정보 수정 API")
    inner class Test3 {
        @Test
        @DisplayName("성공 - 사용자 정보 수정")
        fun success() {
            val modify = mapOf(
                "email" to "새 이메일",
                "password" to "새 비밀번호",
                "name" to "새 이름",
                "nickname" to "새 닉네임",
                "age" to 27,
                "github" to "newGithub",
                "image" to "newImage"
            )

            val response = UserMyPageResponse.fromEntity(user)

            whenever(userMyPageService.modifyUser(
                eq(user.id),
                any<UserModify>()
            )).thenReturn(response)

            mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(modify))
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("개인 정보 수정이 완료되었습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }
}*/
