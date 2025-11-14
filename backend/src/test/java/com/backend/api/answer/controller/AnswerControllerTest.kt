package com.backend.api.answer.controller

import com.backend.api.global.JwtTest
import com.backend.domain.answer.entity.Answer
import com.backend.domain.answer.repository.AnswerRepository
import com.backend.domain.question.entity.Question
import com.backend.domain.question.repository.QuestionRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnswerControllerTest(
    @Autowired
    val mvc: MockMvc,
    @Autowired
    val questionRepository: QuestionRepository,
    @Autowired
    val answerRepository: AnswerRepository
) : JwtTest() {

    private var questionId: Long = 0
    private var answerIdList: List<Long> = emptyList()
    private var userIdList: List<Long> = emptyList()

    @BeforeEach
    @Transactional
    fun setUp() {
        val generalUser = User.builder()
            .email("general@user.com")
            .password("asdf1234!")
            .name("홍길동")
            .nickname("gildong")
            .age(20)
            .github("abc123")
            .image(null)
            .role(Role.USER)
            .build()

        val generalUser2 = User.builder()
            .email("general2@user.com")
            .password("asdf1234!")
            .name("홍길똥")
            .nickname("gilddong")
            .age(25)
            .github("abc1233")
            .image(null)
            .role(Role.USER)
            .build()

        userRepository.save(generalUser)
        userRepository.save(generalUser2)
        userIdList = listOf(generalUser.id!!, generalUser2.id!!)

        val question1 = Question.builder()
            .title("첫 번째 질문 제목")
            .content("첫 번째 질문 내용")
            .author(mockUser)
            .build()
        questionRepository.save(question1)
        questionId = question1.id!!

        val answers = listOf(
            Answer("첫 번째 답변 내용", true, mockUser, question1),
            Answer("두 번째 답변 내용", false, userRepository.findById(userIdList[0]).orElseThrow(), question1),
            Answer("세 번째 답변 내용", true, userRepository.findById(userIdList[0]).orElseThrow(), question1),
            Answer("네 번째 답변 내용", true, mockUser, question1),
            Answer("다섯 번째 답변 내용", true, mockUser, question1),
            Answer("여섯 번째 답변 내용", true, mockUser, question1),
            Answer("일곱 번째 답변 내용", true, mockUser, question1),
            Answer("여덟 번째 답변 내용", true, mockUser, question1),
            Answer("아홉 번째 답변 내용", true, mockUser, question1),
            Answer("열 번째 답변 내용", true, mockUser, question1),
            Answer("열한 번째 답변 내용", true, mockUser, question1),
            Answer("열두 번째 답변 내용", true, mockUser, question1),
            Answer("열세 번째 답변 내용", true, userRepository.findById(userIdList[0]).orElseThrow(), question1)
        )

        answerIdList = answers.map { answerRepository.save(it).id!! }
    }

    @Nested
    @DisplayName("답변 생성 테스트")
    inner class CreateAnswerTest {

        @Test
        @DisplayName("1번 질문에 생성 성공")
        fun success() {
            val answer = "새로운 답변 내용입니다."
            val initialAnswerCount = answerRepository.count()

            val resultActions = mvc.perform(
                post("/api/v1/questions/$questionId/answers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "content": "$answer",
                            "isPublic": true
                        }
                    """.trimIndent())
            ).andDo(print())

            val newAnswers = answerRepository.findAll()
            assertThat(newAnswers).hasSize((initialAnswerCount + 1).toInt())

            val createdAnswer = newAnswers.first {
                it.content == answer && it.question.id == questionId
            }

            resultActions
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("createAnswer"))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("${createdAnswer.id}번 답변이 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(createdAnswer.id))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.content").value(answer))
                .andExpect(jsonPath("$.data.isPublic").value(true))
                .andExpect(jsonPath("$.data.authorId").value(createdAnswer.author.id))
                .andExpect(jsonPath("$.data.authorNickName").value(createdAnswer.author.nickname))
                .andExpect(jsonPath("$.data.questionId").value(questionId))
        }

        @Test
        @DisplayName("필수 필드 누락")
        fun fail1() {
            mvc.perform(
                post("/api/v1/questions/$questionId/answers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"isPublic": true}""")
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("createAnswer"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("답변 내용을 입력해주세요."))
        }

        @Test
        @DisplayName("필수 필드 누락 2")
        fun fail1_2() {
            val answer = "새로운 답변 내용입니다."

            mvc.perform(
                post("/api/v1/questions/$questionId/answers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"content": "$answer"}""")
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("createAnswer"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("답변 공개 여부를 선택해주세요."))
        }

        @Test
        @DisplayName("존재하지 않는 질문에 답변 생성 시도")
        fun fail2() {
            val nonExistentQuestionId = 9999L
            val answer = "새로운 답변 내용입니다."

            mvc.perform(
                post("/api/v1/questions/$nonExistentQuestionId/answers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "content": "$answer",
                            "isPublic": true
                        }
                    """.trimIndent())
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("createAnswer"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."))
        }

        @Test
        @DisplayName("작성자 정보 저장 확인")
        fun success2() {
            val answer = "새로운 답변 내용입니다."

            val resultActions = mvc.perform(
                post("/api/v1/questions/$questionId/answers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "content": "$answer",
                            "isPublic": true
                        }
                    """.trimIndent())
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("createAnswer"))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.status").value("CREATED"))

            val responseBody = resultActions.andReturn().response.contentAsString
            val answerId = JsonPath.parse(responseBody).read("$.data.id", Long::class.java)

            val savedAnswer = answerRepository.findById(answerId).orElseThrow()
            assertThat(savedAnswer.author.id).isEqualTo(mockUser.id)
        }
    }

    @Nested
    @DisplayName("답변 수정 테스트")
    inner class UpdateAnswerTest {

        @Test
        @DisplayName("1번 답변 수정 성공")
        fun success() {
            val targetAnswerId = answerIdList[0]
            val content = "수정된 답변 내용입니다."

            val initialModifyDate = answerRepository.findById(targetAnswerId)
                .orElseThrow()
                .modifyDate

            Thread.sleep(100)

            mvc.perform(
                patch("/api/v1/questions/$questionId/answers/$targetAnswerId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "content": "$content",
                            "isPublic": false
                        }
                    """.trimIndent())
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("updateAnswer"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("${targetAnswerId}번 답변이 수정되었습니다."))
                .andExpect(jsonPath("$.data.id").value(targetAnswerId))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.isPublic").value(false))
                .andExpect(jsonPath("$.data.authorId").value(mockUser.id))
                .andExpect(jsonPath("$.data.authorNickName").value(mockUser.nickname))
                .andExpect(jsonPath("$.data.questionId").value(questionId))

            answerRepository.flush()

            val updatedAnswer = answerRepository.findById(targetAnswerId).orElseThrow()
            assertThat(updatedAnswer.content).isEqualTo(content)
            assertThat(updatedAnswer.question.id).isEqualTo(questionId)
            assertThat(updatedAnswer.author.id).isEqualTo(mockUser.id)
            assertThat(updatedAnswer.modifyDate).isAfter(updatedAnswer.createDate)
            assertThat(updatedAnswer.modifyDate).isAfter(initialModifyDate)
        }

        @Test
        @DisplayName("답변 수정 - 다른 작성자의 답변 수정 시도")
        fun fail1() {
            val targetAnswerId = answerIdList[2]
            val content = "수정된 답변 내용입니다."

            mvc.perform(
                patch("/api/v1/questions/$questionId/answers/$targetAnswerId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "content": "$content",
                            "isPublic": false
                        }
                    """.trimIndent())
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("updateAnswer"))
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("해당 답변에 대한 권한이 없는 사용자입니다."))
        }

        @Test
        @DisplayName("답변 수정 - 없는 답변에 대한 수정 요청")
        fun fail2() {
            val nonExistentAnswerId = 9999L
            val content = "수정된 답변 내용입니다."

            mvc.perform(
                patch("/api/v1/questions/$questionId/answers/$nonExistentAnswerId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "content": "$content",
                            "isPublic": false
                        }
                    """.trimIndent())
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("updateAnswer"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 답변입니다."))
        }
    }

    @Nested
    @DisplayName("답변 삭제 테스트")
    inner class DeleteAnswerTest {

        @Test
        @DisplayName("답변 삭제 - 1번 질문의 1번 답변 삭제")
        fun success() {
            val targetAnswerId = answerIdList[0]

            mvc.perform(
                delete("/api/v1/questions/$questionId/answers/$targetAnswerId")
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("deleteAnswer"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("${targetAnswerId}번 답변이 삭제되었습니다."))

            val answer = answerRepository.findById(targetAnswerId).orElse(null)
            assertThat(answer).isNull()
        }

        @Test
        @DisplayName("답변 삭제 - 다른 사용자의 답변 삭제 시도")
        fun fail1() {
            val targetAnswerId = answerIdList[2]

            mvc.perform(
                delete("/api/v1/questions/$questionId/answers/$targetAnswerId")
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("deleteAnswer"))
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("해당 답변에 대한 권한이 없는 사용자입니다."))

            val answer = answerRepository.findById(targetAnswerId).orElse(null)
            assertThat(answer).isNotNull()
        }
    }

    @Nested
    @DisplayName("답변 목록 조회 테스트")
    inner class ReadAnswersTest {

        @Test
        @DisplayName("답변 목록 조회")
        fun success() {
            mvc.perform(
                get("/api/v1/questions/$questionId/answers")
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("readAnswers"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("${questionId}번 질문의 답변 목록 조회 성공"))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.data.answers[*].id",
                    containsInRelativeOrder(answerIdList.last().toInt(), answerIdList.last().toInt() - 9)))
                .andExpect(jsonPath("$.data.answers[0].id").value(answerIdList.last()))
                .andExpect(jsonPath("$.data.answers[0].createDate").exists())
                .andExpect(jsonPath("$.data.answers[0].modifyDate").exists())
                .andExpect(jsonPath("$.data.answers[0].content").value("열세 번째 답변 내용"))
                .andExpect(jsonPath("$.data.answers[0].isPublic").value(true))
                .andExpect(jsonPath("$.data.answers[0].authorId").value(userIdList[0]))
                .andExpect(jsonPath("$.data.answers[0].authorNickName").value("gildong"))
                .andExpect(jsonPath("$.data.answers[0].questionId").value(questionId))
                .andExpect(jsonPath("$.data.answers[*].isPublic").value(not(hasItem(false))))
                .andExpect(jsonPath("$.data.answers[*].id").value(not(hasItem(2))))
        }

        @Test
        @DisplayName("답변 목록 조회, 2페이지")
        fun success2() {
            mvc.perform(
                get("/api/v1/questions/$questionId/answers?page=2")
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("readAnswers"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("${questionId}번 질문의 답변 목록 조회 성공"))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.data.answers[*].id",
                    containsInRelativeOrder(answerIdList.first().toInt() + 2, answerIdList.first().toInt())))
                .andExpect(jsonPath("$.data.answers[0].id").value(answerIdList.first().toInt() + 2))
                .andExpect(jsonPath("$.data.answers[0].createDate").exists())
                .andExpect(jsonPath("$.data.answers[0].modifyDate").exists())
                .andExpect(jsonPath("$.data.answers[0].content").value("세 번째 답변 내용"))
                .andExpect(jsonPath("$.data.answers[0].isPublic").value(true))
                .andExpect(jsonPath("$.data.answers[0].authorId").value(userIdList[0]))
                .andExpect(jsonPath("$.data.answers[0].authorNickName").value("gildong"))
                .andExpect(jsonPath("$.data.answers[0].questionId").value(questionId))
                .andExpect(jsonPath("$.data.answers[*].isPublic").value(not(hasItem(false))))
                .andExpect(jsonPath("$.data.answers[*].id").value(not(hasItem(2))))
        }

        @Test
        @DisplayName("답변 목록 조회 - 존재하지 않는 질문")
        fun fail1() {
            val nonExistentQuestionId = 9999L

            mvc.perform(
                get("/api/v1/questions/$nonExistentQuestionId/answers")
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("readAnswers"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."))
        }
    }

    @Nested
    @DisplayName("답변 단건 조회 테스트")
    inner class ReadMyAnswerTest {

        @Test
        @DisplayName("내 답변 조회")
        fun success() {
            mvc.perform(
                get("/api/v1/questions/$questionId/answers/mine")
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("readMyAnswer"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("${questionId}번 질문의 내 답변 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(answerIdList[0]))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.content").value("첫 번째 답변 내용"))
                .andExpect(jsonPath("$.data.isPublic").value(true))
                .andExpect(jsonPath("$.data.authorId").value(mockUser.id))
                .andExpect(jsonPath("$.data.authorNickName").value(mockUser.nickname))
                .andExpect(jsonPath("$.data.questionId").value(questionId))
        }

        @Test
        @DisplayName("내 답변 조회 - 존재하지 않는 질문의 답변 조회 시도")
        fun fail3() {
            val nonExistentQuestionId = 9999L

            mvc.perform(
                get("/api/v1/questions/$nonExistentQuestionId/answers/mine")
            ).andDo(print())
                .andExpect(handler().handlerType(AnswerController::class.java))
                .andExpect(handler().methodName("readMyAnswer"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."))
        }
    }
}