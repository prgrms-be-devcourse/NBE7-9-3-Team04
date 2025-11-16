package com.backend.api.question.controller

import com.backend.api.global.JwtTest
import com.backend.domain.question.entity.Question
import com.backend.domain.question.entity.Question.Companion.builder
import com.backend.domain.question.entity.QuestionCategoryType
import com.backend.domain.question.repository.QuestionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // security filter disable
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AiQuestionControllerTest(
    private val mockMvc: MockMvc,
    private val questionRepository: QuestionRepository,
    private var questionId: Long? = null,
    private var groupId: UUID? = null
) : JwtTest() {


    @BeforeEach
    fun setUp() {
        val uuid = UUID.randomUUID()
        var question1 = builder()
            .title("기존 제목")
            .content("기존 내용1")
            .author(mockUser)
            .categoryType(QuestionCategoryType.PORTFOLIO)
            .groupId(uuid)
            .build()
        question1 = questionRepository.save<Question>(question1)
        questionId = question1.id
        groupId = question1.groupId

        val question2 = builder()
            .title("기존 제목")
            .content("기존 내용2")
            .author(mockUser)
            .categoryType(QuestionCategoryType.PORTFOLIO)
            .groupId(uuid)
            .build()
        questionRepository.save<Question?>(question2)

        val question3 = builder()
            .title("기존 제목")
            .content("기존 내용3")
            .author(mockUser)
            .categoryType(QuestionCategoryType.PORTFOLIO)
            .groupId(uuid)
            .build()
        questionRepository.save<Question?>(question3)

        val question4 = builder()
            .title("기존 제목")
            .content("기존 내용4")
            .author(mockUser)
            .categoryType(QuestionCategoryType.PORTFOLIO)
            .groupId(uuid)
            .build()
        questionRepository.save<Question?>(question4)

        val question5 = builder()
            .title("기존 제목")
            .content("기존 내용5")
            .author(mockUser)
            .categoryType(QuestionCategoryType.PORTFOLIO)
            .groupId(uuid)
            .build()
        questionRepository.save<Question?>(question5)
    }

    @Nested
    @DisplayName("AI 면접 질문 조회 API")
    internal inner class t1 {
        @Test
        @DisplayName("정상 작동")
        @Throws(Exception::class)
        fun success() {
            //given
            val question = questionRepository.findById(questionId!!).get()
            val response =
                questionRepository.getQuestionByCategoryTypeAndUserId(QuestionCategoryType.PORTFOLIO, mockUser)

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/ai/questions")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(AiQuestionController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("readAllAiQuestion"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("AI 면접 질문 목록이 조회되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.questions[0].groupId").value(groupId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.questions[0].title").value(question.title))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.questions[0].count").value(response?.questions?.get(0)?.count)
                )
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("유저가 존재하지 않을 때")
        @Throws(Exception::class)
        fun fail1() {
            // given
            userRepository.deleteAll()
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/ai/questions")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(AiQuestionController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("readAllAiQuestion"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("그룹별 AI 면접 질문 조회 API")
    internal inner class t2 {
        @Test
        @DisplayName("정상 작동")
        @Throws(Exception::class)
        fun success() {
            //given
            val question = questionRepository.findById(questionId!!).get()
            val response =
                questionRepository.getQuestionByCategoryTypeAndUserId(QuestionCategoryType.PORTFOLIO, mockUser)

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/ai/questions/${groupId}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(AiQuestionController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("readAiQuestion"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("그룹별 AI 면접 질문 목록이 조회되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(question.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.count").value(response?.questions?.get(0)?.count))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.questions[0].id").value(question.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.questions[0].content").value(question.content))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("유저가 존재하지 않을 때")
        @Throws(Exception::class)
        fun fail1() {
            // given
            userRepository.deleteAll()
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/ai/questions/${groupId}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(AiQuestionController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("readAiQuestion"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }
}