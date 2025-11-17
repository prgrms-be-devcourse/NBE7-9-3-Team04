package com.backend.api.question.controller

import com.backend.api.global.JwtTest

import com.backend.domain.question.entity.Question.Companion.builder
import com.backend.domain.question.entity.QuestionCategoryType
import com.backend.domain.question.repository.QuestionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
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
) : JwtTest() {

    private var questionId: Long = 1
    private lateinit var groupId: UUID

    @BeforeEach
    fun setUp() {
        val uuid = UUID.randomUUID()
        repeat(5) { index ->
            val question = builder()
                .title("기존 제목")
                .content("기존 내용${index + 1}")
                .author(mockUser)
                .categoryType(QuestionCategoryType.PORTFOLIO)
                .groupId(uuid)
                .build()

            val saved = questionRepository.save(question)

            if (index == 0) {
                questionId = saved.id
                groupId = saved.groupId!!
            }
        }
    }

    @Nested
    @DisplayName("AI 면접 질문 조회 API")
    inner class t1 {
        @Test
        @DisplayName("정상 작동")
        fun success() {
            val question = questionRepository.findByIdOrNull(questionId)
            val response = questionRepository.getQuestionByCategoryTypeAndUserId(
                QuestionCategoryType.PORTFOLIO,
                mockUser
            )

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/ai/questions")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(AiQuestionController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("readAllAiQuestion"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("AI 면접 질문 목록이 조회되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.questions[0].groupId").value(groupId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.questions[0].title").value(question?.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.questions[0].count")
                    .value(response?.questions?.get(0)?.count))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("유저가 존재하지 않을 때")
        fun fail1() {
            userRepository.deleteAll()

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/ai/questions")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(AiQuestionController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("readAllAiQuestion"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("그룹별 AI 면접 질문 조회 API")
    inner class t2 {
        @Test
        @DisplayName("정상 작동")
        fun success() {
            val question = questionRepository.findByIdOrNull(questionId)
            val response = questionRepository.getQuestionByCategoryTypeAndUserId(
                QuestionCategoryType.PORTFOLIO,
                mockUser
            )

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/ai/questions/${groupId}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(AiQuestionController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("readAiQuestion"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("그룹별 AI 면접 질문 목록이 조회되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(question?.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.count").value(response?.questions?.get(0)?.count))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.questions[0].id").value(question?.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.questions[0].content").value(question?.content))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("유저가 존재하지 않을 때")
        fun fail1() {
            userRepository.deleteAll()

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/ai/questions/${groupId}")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(AiQuestionController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("readAiQuestion"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }
}
