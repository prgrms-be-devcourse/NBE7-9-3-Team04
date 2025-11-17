package com.backend.api.question.controller

import com.backend.api.global.JwtTest
import com.backend.api.question.dto.request.AdminQuestionAddRequest
import com.backend.api.question.dto.request.AdminQuestionUpdateRequest
import com.backend.api.question.dto.request.QuestionApproveRequest
import com.backend.api.question.dto.request.QuestionScoreRequest
import com.backend.domain.question.entity.Question
import com.backend.domain.question.entity.QuestionCategoryType
import com.backend.domain.question.repository.QuestionRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AdminQuestionControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val questionRepository: QuestionRepository,
    override var userRepository: UserRepository,
) : JwtTest() {

    @MockBean
    private lateinit var rq: Rq

    private lateinit var testUser: User
    private lateinit var savedQuestion: Question

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(
            User.builder()
                .name("Admin")
                .email("admin@test.com")
                .github("https://github.com/admin")
                .password("1234")
                .nickname("admin")
                .role(Role.ADMIN)
                .build()
        )

        Mockito.`when`(rq.getUser()).thenReturn(testUser)

        val question = Question.builder()
            .title("기존 제목")
            .content("기존 내용")
            .author(testUser)
            .categoryType(QuestionCategoryType.DATABASE)
            .build()

        savedQuestion = questionRepository.save(question)
    }

    @Nested
    @DisplayName("관리자용 질문 생성 API")
    inner class T1 {

        @Test
        @DisplayName("질문 생성 성공")
        fun success() {
            val request = AdminQuestionAddRequest(
                "Spring Boot란?",
                "Spring Boot의 핵심 개념과 장점을 설명해주세요.",
                QuestionCategoryType.OS,
                true,
                5
            )

            mockMvc.perform(
                post("/api/v1/admin/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("질문이 생성되었습니다."))
                .andExpect(jsonPath("$.data.title").value("Spring Boot란?"))
                .andExpect(
                    jsonPath("$.data.content")
                        .value("Spring Boot의 핵심 개념과 장점을 설명해주세요.")
                )
                .andExpect(jsonPath("$.data.score").value(5))
                .andExpect(jsonPath("$.data.isApproved").value(true))
                .andExpect(jsonPath("$.data.authorId").value(testUser.id))
                .andExpect(jsonPath("$.data.authorNickname").value("admin"))
                .andExpect(jsonPath("$.data.categoryType").value("OS"))
                .andDo(print())
        }

        @Test
        @DisplayName("질문 생성 실패 - 제목 누락")
        fun fail1() {
            val request = AdminQuestionAddRequest(
                "", // 제목 누락
                "내용은 있습니다.",
                QuestionCategoryType.OS,
                true,
                0
            )

            mockMvc.perform(
                post("/api/v1/admin/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("질문 제목은 필수입니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("질문 생성 실패 - 내용 누락")
        fun fail2() {
            val request = AdminQuestionAddRequest(
                "Spring Boot란?",
                "", // 내용 누락
                QuestionCategoryType.OS,
                true,
                0
            )

            mockMvc.perform(
                post("/api/v1/admin/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("질문 내용은 필수입니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("질문 생성 실패 - 점수가 음수")
        fun fail3() {
            val request = AdminQuestionAddRequest(
                "Spring Boot란?",
                "Spring Boot의 핵심 개념과 장점을 설명해주세요.",
                QuestionCategoryType.OS,
                true,
                -3 // 점수가 음수일 때
            )

            mockMvc.perform(
                post("/api/v1/admin/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("점수는 0 이상이어야 합니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("관리자용 질문 수정 API")
    inner class T2 {

        @Test
        @DisplayName("질문 수정 성공")
        fun success() {
            val questionId = savedQuestion.id
            val request = AdminQuestionUpdateRequest(
                "관리자 수정 제목",
                "관리자 수정 내용",
                true,
                10,
                QuestionCategoryType.DATABASE
            )

            mockMvc.perform(
                put("/api/v1/admin/questions/{questionId}", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("질문이 수정되었습니다."))
                .andExpect(jsonPath("$.data.title").value("관리자 수정 제목"))
                .andExpect(jsonPath("$.data.content").value("관리자 수정 내용"))
                .andExpect(jsonPath("$.data.authorId").value(testUser.id))
                .andExpect(jsonPath("$.data.authorNickname").value("admin"))
                .andExpect(jsonPath("$.data.categoryType").value("DATABASE"))
                .andDo(print())
        }

        @Test
        @DisplayName("질문 수정 실패 - 존재하지 않는 ID")
        fun fail1() {
            val questionId = 999L
            val request = AdminQuestionUpdateRequest(
                "수정 제목",
                "수정 내용",
                true,
                10,
                QuestionCategoryType.DATABASE
            )

            mockMvc.perform(
                put("/api/v1/admin/questions/{questionId}", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("질문 수정 실패 - 제목 누락")
        fun fail2() {
            val questionId = savedQuestion.id
            val request = AdminQuestionUpdateRequest(
                "",
                "내용만 있습니다.",
                true,
                10,
                QuestionCategoryType.OS
            )

            mockMvc.perform(
                put("/api/v1/admin/questions/{questionId}", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("질문 제목은 필수입니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("질문 수정 실패 - 내용 누락")
        fun fail3() {
            val questionId = savedQuestion.id
            val request = AdminQuestionUpdateRequest(
                "제목만 있습니다.",
                "",
                true,
                10,
                QuestionCategoryType.OS
            )

            mockMvc.perform(
                put("/api/v1/admin/questions/{questionId}", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("질문 내용은 필수입니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("관리자용 질문 승인 처리 API")
    inner class T3 {

        @Test
        @DisplayName("질문 승인 성공")
        fun approve() {
            val questionId = savedQuestion.id
            val request = QuestionApproveRequest(true)

            mockMvc.perform(
                patch("/api/v1/admin/questions/{questionId}/approve", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("질문이 승인 처리되었습니다."))
                .andExpect(jsonPath("$.data.isApproved").value(true))
                .andDo(print())
        }

        @Test
        @DisplayName("질문 비승인 성공")
        fun disapprove() {
            val questionId = savedQuestion.id
            val request = QuestionApproveRequest(false)

            mockMvc.perform(
                patch("/api/v1/admin/questions/{questionId}/approve", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("질문이 비승인 처리되었습니다."))
                .andExpect(jsonPath("$.data.isApproved").value(false))
                .andDo(print())
        }

        @Test
        @DisplayName("질문 승인 실패 - 존재하지 않는 질문 ID")
        fun fail1() {
            val questionId = 999L
            val request = QuestionApproveRequest(true)

            mockMvc.perform(
                patch("/api/v1/admin/questions/{questionId}/approve", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("관리자용 질문 점수 수정 API")
    inner class T4 {

        @Test
        @DisplayName("질문 점수 수정 성공")
        fun success() {
            val questionId = savedQuestion.id
            val request = QuestionScoreRequest(20)

            mockMvc.perform(
                patch("/api/v1/admin/questions/{questionId}/score", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("질문 점수가 수정되었습니다."))
                .andExpect(jsonPath("$.data.score").value(20))
                .andDo(print())
        }

        @Test
        @DisplayName("질문 점수 수정 실패 - 음수 점수")
        fun fail1() {
            val questionId = savedQuestion.id
            val request = QuestionScoreRequest(-5)

            mockMvc.perform(
                patch("/api/v1/admin/questions/{questionId}/score", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("점수는 0 이상이어야 합니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("질문 점수 수정 실패 - 존재하지 않는 ID")
        fun fail2() {
            val questionId = 999L
            val request = QuestionScoreRequest(5)

            mockMvc.perform(
                patch("/api/v1/admin/questions/{questionId}/score", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("관리자 질문 조회 API (페이징 기반)")
    inner class T5 {

        @Test
        @DisplayName("관리자 질문 목록 조회 성공 - 승인 여부 관계없이 전체 반환 (페이징)")
        fun success() {
            val approved = questionRepository.save(
                Question.builder()
                    .title("승인 질문")
                    .content("승인된 질문 내용")
                    .author(testUser)
                    .categoryType(QuestionCategoryType.DATABASE)
                    .build()
            )
            approved.updateApproved(true)

            val unapproved = questionRepository.save(
                Question.builder()
                    .title("미승인 질문")
                    .content("미승인 질문 내용")
                    .author(testUser)
                    .categoryType(QuestionCategoryType.DATABASE)
                    .build()
            )
            unapproved.updateApproved(false)

            mockMvc.perform(
                get("/api/v1/admin/questions")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("관리자 질문 목록 조회 성공"))
                .andExpect(jsonPath("$.data.questions").isArray)
                .andExpect(jsonPath("$.data.questions[*].title", hasItem("승인 질문")))
                .andExpect(jsonPath("$.data.questions[*].title", hasItem("미승인 질문")))
                .andDo(print())
        }

//        // UserQuestion CascadeType.REMOVE 미설정으로 삭제 불가
//        @Test
//        @DisplayName("관리자 질문 목록 조회 실패 - 데이터 없음")
//        fun fail1() {
//            questionRepository.deleteAll()
//
//            mockMvc.perform(
//                get("/api/v1/admin/questions")
//                    .param("page", "0")
//                    .param("size", "10")
//                    .contentType(MediaType.APPLICATION_JSON)
//            )
//                .andExpect(status().isNotFound)
//                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
//                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."))
//                .andDo(print())
//        }
    }

    @Nested
    @DisplayName("관리자 질문 단건 조회 API")
    inner class T6 {

        @Test
        @DisplayName("관리자 질문 단건 조회 성공 - 승인 여부 관계없이 조회 가능")
        fun success() {
            val question = questionRepository.save(
                Question.builder()
                    .title("관리자용 단건 조회 질문")
                    .content("관리자는 승인 여부 관계없이 조회 가능")
                    .author(testUser)
                    .build()
            )
            question.updateApproved(false)

            mockMvc.perform(
                get("/api/v1/admin/questions/{questionId}", question.id)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("관리자 질문 단건 조회 성공"))
                .andExpect(jsonPath("$.data.questionId").value(question.id))
                .andExpect(jsonPath("$.data.title").value("관리자용 단건 조회 질문"))
                .andExpect(
                    jsonPath("$.data.content")
                        .value("관리자는 승인 여부 관계없이 조회 가능")
                )
                .andDo(print())
        }

        @Test
        @DisplayName("관리자 질문 단건 조회 실패 - 존재하지 않는 ID")
        fun fail1() {
            mockMvc.perform(
                get("/api/v1/admin/questions/{questionId}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("관리자 질문 삭제 API")
    inner class T7 {

        @Test
        @DisplayName("관리자 질문 삭제 성공 - 존재하는 질문 정상 삭제")
        fun success() {
            val question = questionRepository.save(
                Question.builder()
                    .title("관리자용 삭제 테스트 질문")
                    .content("삭제 성공 케이스")
                    .author(testUser)
                    .build()
            )

            mockMvc.perform(
                delete("/api/v1/admin/questions/{questionId}", question.id)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("관리자 질문 삭제 성공"))
                .andDo(print())

            Assertions.assertTrue(questionRepository.findById(question.id).isEmpty)
        }

        @Test
        @DisplayName("관리자 질문 삭제 실패 - 존재하지 않는 ID")
        fun fail1() {
            mockMvc.perform(
                delete("/api/v1/admin/questions/{questionId}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("관리자 질문 삭제 실패 - 일반 사용자는 삭제 불가")
        fun fail2() {
            val normalUser = userRepository.save(
                User.builder()
                    .name("NormalUser")
                    .email("user@test.com")
                    .github("https://github.com/user")
                    .password("1234")
                    .nickname("user")
                    .role(Role.USER)
                    .build()
            )

            Mockito.`when`(rq.getUser()).thenReturn(normalUser)

            val question = questionRepository.save(
                Question.builder()
                    .title("관리자용 삭제 테스트 질문")
                    .content("삭제 불가 케이스")
                    .author(testUser)
                    .build()
            )

            mockMvc.perform(
                delete("/api/v1/admin/questions/{questionId}", question.id)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
                .andDo(print())
        }
    }
}
