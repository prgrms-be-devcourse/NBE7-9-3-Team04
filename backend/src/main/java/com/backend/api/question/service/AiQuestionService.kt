package com.backend.api.question.service


import com.backend.api.question.dto.request.AiQuestionRequest
import com.backend.api.question.dto.response.*
import com.backend.api.resume.service.ResumeService
import com.backend.api.review.dto.request.AiReviewbackRequest
import com.backend.api.user.service.UserService
import com.backend.domain.question.entity.Question
import com.backend.domain.question.entity.QuestionCategoryType
import com.backend.domain.resume.entity.Resume
import com.backend.domain.user.entity.User
import com.backend.global.ai.handler.AiRequestHandler
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AiQuestionService(
    private val questionService: QuestionService,
    private val userService: UserService,
    private val resumeService: ResumeService,
    private val objectMapper: ObjectMapper,
    private val aiRequestHandler: AiRequestHandler

) {

    fun createAiQuestion(userId: Long): AIQuestionCreateResponse {
        val user = userService.getUser(userId)
        // AI 질문 생성 횟수 제한 검증
        validateQuestionLimit(user)

        val resume = resumeService.getResumeByUser(user)
        val request = AiQuestionRequest.of(resume.skill, resume.portfolioUrl, user.getAiQuestionLimit())

        val connectionAi = aiRequestHandler.connectionAi(request)

        val responses = parseChatGptResponse(connectionAi)

        val questions = listDtoToEntity(responses, user, resume)

        questionService.createListQuestion(questions)
        val questionDto = AiQuestionResponse.toDtoList(questions)
        return AIQuestionCreateResponse.from(questions.first().groupId, questionDto)
    }

    @Transactional(readOnly = true)
    fun getAiReviewContent(request: AiReviewbackRequest): String {
        val rawApiResponse = aiRequestHandler.connectionAi(request)
        return parseSingleContentFromResponse(rawApiResponse)
    }

    // AI 질문 생성 횟수 제한 검증
    fun validateQuestionLimit(user: User) {
        val availableCount = user.getAiQuestionLimit()
        val usedCount = user.aiQuestionUsedCount

        if (usedCount >= availableCount) {
            throw ErrorException(ErrorCode.AI_QUESTION_LIMIT_EXCEEDED)
        }
        user.incrementAiQuestionUsedCount()
    }

    fun parseChatGptResponse(connectionAi: String): List<AiQuestionResponse> {
        val responseDto = objectMapper.readValue(connectionAi, ChatGptResponse::class.java)

        val content: String = responseDto.choiceResponses.first().message.content

        val cleanJson = content
            .replace("```json", "")
            .replace("\n", "")
            .trim()

        return objectMapper.readValue(cleanJson)
    }



    fun parseSingleContentFromResponse(rawApiResponse: String): String {
        val responseDto = objectMapper.readValue(rawApiResponse, ChatGptResponse::class.java)
        return responseDto.choiceResponses.first().message.content
    }


    fun listDtoToEntity(
        responses: List<AiQuestionResponse>,
        user: User,
        resume: Resume
    ): List<Question> {
        val groupId = UUID.randomUUID()
        return responses.map { dto ->
            val content = dto.content
                .takeIf { it.isBlank() }
                ?: throw ErrorException(ErrorCode.NOT_FOUND_CONTENT)

            Question(
                resume.portfolioUrl ?: "포트폴리오 URL 없음",
                content,
                true,
                2,
                user,
                QuestionCategoryType.PORTFOLIO,
                groupId
            )
        }
    }


    @Transactional(readOnly = true)
    fun readAllAiQuestion(userId: Long): AiQuestionReadAllResponse {
        val user = userService.getUser(userId)
        return questionService.getByCategoryType(QuestionCategoryType.PORTFOLIO, user)
    }

    @Transactional(readOnly = true)
    fun readAiQuestion(userId: Long, groupId: UUID): PortfolioListReadResponse {
        val user = userService.getUser(userId)
        return questionService.getByUserAndGroupId(user, groupId)
    }
}
