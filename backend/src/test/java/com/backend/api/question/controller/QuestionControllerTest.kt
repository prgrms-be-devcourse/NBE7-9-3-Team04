package com.backend.api.question.controller

import com.backend.api.global.JwtTest
import com.backend.api.question.dto.request.QuestionAddRequest
import com.backend.api.question.dto.request.QuestionUpdateRequest
import com.backend.domain.question.entity.Question
import com.backend.domain.question.entity.QuestionCategoryType
import com.backend.domain.question.repository.QuestionRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.everyItem
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class QuestionControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val questionRepository: QuestionRepository,
    override var userRepository: UserRepository
) : JwtTest() {

    @MockBean
    private lateinit var rq: Rq

    private lateinit var testUser: User
    private lateinit var savedQuestion: Question

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(
            User.builder()
                .name("TestUser")
                .email("test@test.com")
                .github("https://github.com/tester")
                .password("1234")
                .nickname("tester")
                .role(Role.USER)
                .build()
        )

        Mockito.`when`(rq.getUser()).thenReturn(testUser)

        val question = Question.builder()
            .title("기존 제목")
            .content("기존 내용")
            .author(testUser)
            .categoryType(QuestionCategoryType.ALGORITHM)
            .build()

        savedQuestion = questionRepository.save(question)
    }

    @Nested
    @DisplayName("질문 생성 API")
    inner class T1 {

        @Test
        @DisplayName("질문 생성 성공")
        fun success() {
            val request = QuestionAddRequest(
                "Spring Boot란?",
                "Spring Boot의 핵심 개념과 장점을 설명해주세요.",
                QuestionCategoryType.DATABASE
            )

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("질문이 생성되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("Spring Boot란?"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.content")
                        .value("Spring Boot의 핵심 개념과 장점을 설명해주세요.")
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.authorId")
                        .value(testUser.id)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.authorNickname")
                        .value("tester")
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.categoryType")
                        .value("DATABASE")
                )
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("질문 생성 실패 - 제목 누락")
        fun fail1() {
            val request = QuestionAddRequest(
                "", // 제목 누락
                "내용입니다.",
                QuestionCategoryType.OS
            )

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("질문 제목은 필수입니다.")
                )
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("질문 생성 실패 - 내용 누락")
        fun fail2() {
            val request = QuestionAddRequest(
                "Spring Boot란?",
                "",
                QuestionCategoryType.OS
            )

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("질문 내용은 필수입니다.")
                )
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("질문 수정 API")
    inner class T2 {

        @Test
        @DisplayName("질문 수정 성공")
        fun success() {
            val questionId = savedQuestion.id
            val request = QuestionUpdateRequest(
                "수정된 제목",
                "수정된 내용",
                QuestionCategoryType.OS
            )

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/questions/{questionId}", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("질문이 수정되었습니다.")
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.title")
                        .value("수정된 제목")
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.content")
                        .value("수정된 내용")
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.categoryType")
                        .value("OS")
                )
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("질문 수정 실패 - 존재하지 않는 ID")
        fun fail1() {
            val questionId = 999L
            val request = QuestionUpdateRequest(
                "수정된 제목",
                "수정된 내용",
                QuestionCategoryType.OS
            )

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/questions/{questionId}", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("질문을 찾을 수 없습니다.")
                )
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("질문 수정 실패 - 제목 누락")
        fun fail2() {
            val questionId = savedQuestion.id
            val request = QuestionUpdateRequest(
                "",
                "내용만 있습니다.",
                QuestionCategoryType.OS
            )

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/questions/{questionId}", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("질문 제목은 필수입니다.")
                )
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("질문 수정 실패 - 내용 누락")
        fun fail3() {
            val questionId = savedQuestion.id
            val request = QuestionUpdateRequest(
                "제목만 있습니다.",
                "",
                QuestionCategoryType.OS
            )

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/questions/{questionId}", questionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("질문 내용은 필수입니다.")
                )
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("질문 조회 API")
    inner class T3 {

        @Test
        @DisplayName("질문 목록 조회 성공 - 승인된 질문만 반환")
        fun success() {
            val approvedQuestion = questionRepository.save(
                Question.builder()
                    .title("승인된 질문")
                    .content("승인된 질문 내용")
                    .author(testUser)
                    .categoryType(QuestionCategoryType.NETWORK)
                    .build()
            )
            approvedQuestion.updateApproved(true)

            val unapprovedQuestion = questionRepository.save(
                Question.builder()
                    .title("미승인 질문")
                    .content("미승인 질문 내용")
                    .author(testUser)
                    .categoryType(QuestionCategoryType.NETWORK)
                    .build()
            )
            unapprovedQuestion.updateApproved(false)

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/questions")
                    .contentType(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("질문 목록 조회 성공")
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.questions").isArray
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.questions[0].title")
                        .value("승인된 질문")
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.questions[0].categoryType")
                        .value("NETWORK")
                )
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("카테고리별 질문 조회 성공")
        fun success2() {
            val osQuestion = questionRepository.save(
                Question.builder()
                    .title("운영체제 관련 질문")
                    .content("프로세스와 스레드의 차이를 설명해주세요.")
                    .author(testUser)
                    .categoryType(QuestionCategoryType.OS)
                    .build()
            )
            osQuestion.updateApproved(true)

            val dbQuestion = questionRepository.save(
                Question.builder()
                    .title("DB 관련 질문")
                    .content("인덱스가 언제 비효율적인가요?")
                    .author(testUser)
                    .categoryType(QuestionCategoryType.DATABASE)
                    .build()
            )
            dbQuestion.updateApproved(true)

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/questions/category/{categoryType}", "OS")
                    .contentType(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("카테고리별 질문 조회 성공")
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.questions").isArray
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.questions[*].categoryType",
                        everyItem(`is`("OS")))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.currentPage")
                        .value(1)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.totalPages")
                        .exists()
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.totalCount")
                        .exists()
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.pageSize")
                        .exists()
                )
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("질문 단건 조회 API")
    inner class T4 {

        @Test
        @DisplayName("질문 단건 조회 성공")
        fun success() {
            val question = questionRepository.save(
                Question.builder()
                    .title("상세 질문")
                    .content("상세 질문 내용")
                    .author(testUser)
                    .build()
            )
            question.updateApproved(true)

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/questions/{questionId}", question.id)
                    .contentType(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("질문 단건 조회 성공")
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.questionId")
                        .value(question.id)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.title")
                        .value("상세 질문")
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.content")
                        .value("상세 질문 내용")
                )
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("질문 상세 조회 실패 - 존재하지 않는 ID")
        fun fail1() {
            val invalidId = 999L

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/questions/{questionId}", invalidId)
                    .contentType(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("질문을 찾을 수 없습니다.")
                )
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("질문 상세 조회 실패 - 승인되지 않은 질문 접근")
        fun fail2() {
            val question = questionRepository.save(
                Question.builder()
                    .title("미승인 질문")
                    .content("미승인 질문 내용")
                    .author(testUser)
                    .build()
            )
            question.updateApproved(false)

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/questions/{questionId}", question.id)
                    .contentType(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message")
                        .value("승인되지 않은 질문입니다.")
                )
                .andDo(MockMvcResultHandlers.print())
        }
    }
}
