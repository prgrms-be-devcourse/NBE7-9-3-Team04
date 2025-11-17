package com.backend.api.feedback.service

import com.backend.api.answer.service.AnswerService
import com.backend.api.feedback.dto.request.AiFeedbackRequest
import com.backend.api.feedback.dto.response.AiFeedbackResponse
import com.backend.api.feedback.dto.response.FeedbackReadResponse
import com.backend.api.question.service.QuestionService
import com.backend.api.ranking.service.RankingService
import com.backend.api.userQuestion.service.UserQuestionService
import com.backend.domain.answer.entity.Answer
import com.backend.domain.feedback.entity.Feedback
import com.backend.domain.feedback.repository.FeedbackRepository
import com.backend.domain.question.entity.Question
import com.backend.domain.user.entity.User
import com.backend.global.ai.handler.AiRequestHandler
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import com.backend.global.exception.GlobalExceptionHandler
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.List



@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val questionService: QuestionService,
    private val aiRequestHandler: AiRequestHandler,
    private val answerService: AnswerService,
    private val userQuestionService: UserQuestionService,
    private val rankingService: RankingService,
    private val geminiModel: VertexAiGeminiChatModel,
    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
) {
    @Transactional
    fun createFeedback(answer: Answer) {
        val question = questionService.findByIdOrThrow(answer.question.id)
        val aiFeedback = createAiFeedback(question, answer)

        val feedback =  Feedback(
            aiFeedback.content,
            aiFeedback.score,
            answer
        )
        feedbackRepository.save(feedback)

        val baseScore = question.score
        val ratio = feedback.aiScore / 100.0
        val finalScore = Math.round(ratio * baseScore).toInt()

        userQuestionService.updateUserQuestionScore(answer.author, question, finalScore)
        rankingService.updateUserRanking(answer.author)
    }

    @Transactional
    fun updateFeedback(answer: Answer) {
        val question = questionService.findByIdOrThrow(answer.question.id)
        val aiFeedback = createAiFeedback(question, answer)
        val feedback = getFeedbackByAnswerId(answer.id)
        feedback.update(answer, aiFeedback.score, aiFeedback.content)

        val baseScore = question.score
        val ratio = feedback.aiScore / 100.0
        val finalScore = Math.round(ratio * baseScore).toInt()

        userQuestionService.updateUserQuestionScore(answer.author, question, finalScore)
        rankingService.updateUserRanking(answer.author)
    }

    fun getFeedbackByAnswerId(answerId: Long): Feedback {
        return feedbackRepository.findByAnswerId(answerId)
            ?: throw ErrorException(ErrorCode.FEEDBACK_NOT_FOUND)
    }


    fun createAiFeedback(question: Question, answer: Answer): AiFeedbackResponse {
        // 리퀘스트 dto 정의
        val request = AiFeedbackRequest.of(question.content, answer.content)

        val prompt = createPrompt(request.systemMessage, request.userMessage, request.assistantMessage)

        return connectChatClient(prompt)
    }


    @Retry(name = "aiRetry")
    @CircuitBreaker(name = "aiExternal", fallbackMethod = "fallbackToGemini")
    fun connectChatClient(prompt: Prompt): AiFeedbackResponse {
        return aiRequestHandler.execute { client ->
            client.prompt(prompt)
                .call()
                .entity(AiFeedbackResponse::class.java)
                ?: throw ErrorException(ErrorCode.AI_SERVICE_ERROR)
        }
    }

    fun fallbackToGemini(prompt: Prompt, e: Throwable): AiFeedbackResponse {
        val geminiClient = ChatClient.create(geminiModel)
        logger.warn("[Fallback Triggered] OpenAI unavailable → switching to Gemini. Cause={}", e.message)
        return geminiClient.prompt(prompt)
            .call()
            .entity(AiFeedbackResponse::class.java)
            ?: throw ErrorException(ErrorCode.AI_SERVICE_ERROR)
    }

    fun createPrompt(system: String, user: String, assistant: String): Prompt {
        // role별 메시지
        val systemMessage = SystemMessage(system)
        val userMessage = UserMessage(user)
        val assistantMessage = AssistantMessage(assistant)

        // 옵션
        val options = OpenAiChatOptions.builder()
            .model("gpt-4.1-mini")
            .temperature(1.0)
            .build()

        // 프롬프트
        return Prompt(List.of<Message>(systemMessage, userMessage, assistantMessage), options)
    }


    @Transactional(readOnly = true)
    fun readFeedback(questionId: Long, user: User): FeedbackReadResponse {
        val response = answerService.findMyAnswer(questionId)
        val feedback = getFeedbackByAnswerId(response.id)

        return FeedbackReadResponse.from(feedback)
    }

    fun getFeedback(id: Long): Feedback {
        return feedbackRepository.findByIdOrNull(id)
            ?: throw ErrorException(ErrorCode.FEEDBACK_NOT_FOUND)
    }
}
