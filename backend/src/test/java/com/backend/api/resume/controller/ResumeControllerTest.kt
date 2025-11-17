package com.backend.api.resume.controller

import com.backend.api.global.JwtTest
import com.backend.api.resume.dto.request.ResumeCreateRequest
import com.backend.api.resume.dto.request.ResumeUpdateRequest

import com.backend.domain.resume.entity.Resume
import com.backend.domain.resume.repository.ResumeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder

import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // security filter disable
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ResumeControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val resumeRepository: ResumeRepository
) : JwtTest() {


    @BeforeEach
    fun setUp() {
        val resume = Resume(
            "이력서 내용입니다.",
            "Java, Spring Boot",
            "대외 활동 내용입니다.",
            "없음",
            "경력 사항 내용입니다.",
            "http://portfolio.example.com",
            mockUser
        )
        resumeRepository.save(resume)
    }

    @Nested
    @DisplayName("이력서 생성 API")
    internal inner class t1 {
        @Test
        @DisplayName("정상 작동")
        fun success() {
            // given
            resumeRepository.deleteAll()
            val request = ResumeCreateRequest(
                "이력서 내용입니다.",
                "Java, Spring Boot",
                "대외 활동 내용입니다.",
                "없음",
                "경력 사항 내용입니다.",
                "http://portfolio.example.com"
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/resumes")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("createResume"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("CREATED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이력서가 생성되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(mockUser.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("이력서 내용입니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.skill").value("Java, Spring Boot"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.activity").value("대외 활동 내용입니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.certification").value("없음"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.career").value("경력 사항 내용입니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.portfolioUrl").value("http://portfolio.example.com"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("로그인 안 된 상태에서 요청할 때")
        fun fail1() {
            // given
            SecurityContextHolder.clearContext()
            val request = ResumeCreateRequest(
                "이력서 내용입니다.",
                "Java, Spring Boot",
                "대외 활동 내용입니다.",
                "없음",
                "경력 사항 내용입니다.",
                "http://portfolio.example.com"
            )
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/resumes")

                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("createResume"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("로그인된 사용자가 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("이미 이력서가 존재할 때")
        fun fail2() {
            // given
            val request = ResumeCreateRequest(
                "이력서 내용입니다.",
                "Java, Spring Boot",
                "대외 활동 내용입니다.",
                "없음",
                "경력 사항 내용입니다.",
                "http://portfolio.example.com"
            )
            // when
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/resumes")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/resumes")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("createResume"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이미 등록된 이력서가 있습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("이력서 수정 API")
    internal inner class t2 {
        @Test
        @DisplayName("정상 작동")
        fun success() {
            //given
            val request = ResumeUpdateRequest(
                "수정된 이력서 내용입니다.",
                "Java, Spring Boot, mysql",
                "수정된 대외 활동 내용입니다.",
                "없음",
                "수정된 경력 사항 내용입니다.",
                "http://portfolio.example2.com"
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/users/resumes")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("updateResume"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이력서가 수정되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(mockUser.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("수정된 이력서 내용입니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.skill").value("Java, Spring Boot, mysql"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.activity").value("수정된 대외 활동 내용입니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.certification").value("없음"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.career").value("수정된 경력 사항 내용입니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.portfolioUrl").value("http://portfolio.example2.com"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("이력서가 존재하지 않을 때")
        fun fail1() {
            resumeRepository.deleteAll()
            //given
            val request = ResumeUpdateRequest(
                "수정된 이력서 내용입니다.",
                "Java, Spring Boot, mysql",
                "수정된 대외 활동 내용입니다.",
                "없음",
                "수정된 경력 사항 내용입니다.",
                "http://portfolio.example2.com"
            )
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/users/resumes")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("updateResume"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이력서를 찾을 수 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("이력서 삭제 API")
    internal inner class t3 {
        @Test
        @DisplayName("정상 작동")
        fun success() {
            //given

            val resume: Resume? = resumeRepository.findByUser(mockUser)

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/users/resumes/${resume?.id}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("deleteResume"))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NO_CONTENT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이력서가 삭제되었습니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("이력서가 존재하지 않을 때")
        fun fail1() {
            // given
            val resumeId = 999L
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/users/resumes/${resumeId}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("deleteResume"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이력서를 찾을 수 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("작성자 불일치")
        fun fail2() {
            // given
            val resumeId = 1L
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/users/resumes/${resumeId}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("deleteResume"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이력서 수정 권한이 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("유저가 존재하지 않을 때")
        fun fail4() {
            // given
            val resumeId = 1L
            userRepository.deleteAll()
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/users/resumes/${resumeId}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("deleteResume"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("이력서 조회 API")
    internal inner class t4 {
        @Test
        @DisplayName("정상 작동")
        fun success() {
            //given

            // when

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/users/resumes")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("readResume"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이력서를 조회했습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(mockUser.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("이력서 내용입니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.skill").value("Java, Spring Boot"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.activity").value("대외 활동 내용입니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.certification").value("없음"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.career").value("경력 사항 내용입니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.portfolioUrl").value("http://portfolio.example.com"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("유저가 존재하지 않을 때")
        fun fail1() {
            // given
            userRepository.deleteAll()
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/users/resumes")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(ResumeController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("readResume"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }
}