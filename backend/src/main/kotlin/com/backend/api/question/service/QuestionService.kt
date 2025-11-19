package com.backend.api.question.service

import com.backend.api.question.dto.request.QuestionAddRequest
import com.backend.api.question.dto.request.QuestionUpdateRequest
import com.backend.api.question.dto.response.AiQuestionReadAllResponse
import com.backend.api.question.dto.response.PortfolioListReadResponse
import com.backend.api.question.dto.response.QuestionPageResponse
import com.backend.api.question.dto.response.QuestionResponse
import com.backend.api.user.service.UserService
import com.backend.domain.question.entity.Question
import com.backend.domain.question.entity.QuestionCategoryType
import com.backend.domain.question.repository.QuestionRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.global.Rq.Rq
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val userService: UserService,
    private val rq: Rq
) {

    // 권한 검증
    private fun requireUser(user: User): User {
        if (user.role != Role.USER) throw ErrorException(ErrorCode.FORBIDDEN)
        return user
    }

    fun findByIdOrThrow(questionId: Long): Question =
        questionRepository.findByIdOrNull(questionId)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_QUESTION)

    private fun validateQuestionAuthor(question: Question, user: User) {
        if (question.author.id != user.id) {
            throw ErrorException(ErrorCode.FORBIDDEN)
        }
    }

    private fun validateApprovedQuestion(question: Question) {
        if (!question.isApproved) {
            throw ErrorException(ErrorCode.QUESTION_NOT_APPROVED)
        }
    }

    @Transactional
    fun addQuestion(@Valid request: QuestionAddRequest, user: User): QuestionResponse {
        val u = requireUser(user)

        val question = Question(
            title = request.title,
            content = request.content,
            author = u,
            categoryType = request.categoryType
        )

        val saved = questionRepository.save(question)
        return QuestionResponse.from(saved)
    }

    @Transactional
    fun updateQuestion(questionId: Long, @Valid request: QuestionUpdateRequest, user: User): QuestionResponse {
        val u = requireUser(user)

        val question = findByIdOrThrow(questionId)
        validateQuestionAuthor(question, u)

        question.updateUserQuestion(
            request.title,
            request.content,
            request.categoryType
        )

        return QuestionResponse.from(question)
    }

    fun getApprovedQuestions(page: Int, categoryType: QuestionCategoryType?): QuestionPageResponse<QuestionResponse> {
        val pageNum = maxOf(page, 1)
        val pageable: Pageable = PageRequest.of(pageNum - 1, 9, Sort.by("createDate").descending())

        val questionsPage: Page<Question> = when (categoryType) {
            null ->
                questionRepository.findApprovedQuestionsExcludingCategory(
                    QuestionCategoryType.PORTFOLIO,
                    pageable
                )

            QuestionCategoryType.PORTFOLIO ->
                questionRepository.findApprovedQuestionsByCategory(
                    QuestionCategoryType.PORTFOLIO,
                    pageable
                )

            else ->
                questionRepository.findByCategoryTypeAndIsApprovedTrue(categoryType, pageable)
        }

        if (questionsPage.isEmpty) throw ErrorException(ErrorCode.NOT_FOUND_QUESTION)

        val responses = questionsPage.content.map { QuestionResponse.from(it) }
        return QuestionPageResponse.from(questionsPage, responses)
    }

    fun getApprovedQuestionById(questionId: Long): QuestionResponse {
        val question = findByIdOrThrow(questionId)
        validateApprovedQuestion(question)
        return QuestionResponse.from(question)
    }

    fun getNotApprovedQuestionById(userId: Long, questionId: Long, user: User): QuestionResponse {
        val u = requireUser(user)

        if (u.id != userId) throw ErrorException(ErrorCode.QUESTION_INVALID_USER)

        val question = findByIdOrThrow(questionId)
        validateQuestionAuthor(question, u)

        if (question.isApproved) {
            throw ErrorException(ErrorCode.ALREADY_APPROVED_QUESTION)
        }

        return QuestionResponse.from(question)
    }

    @Transactional
    fun createListQuestion(questions: List<Question>) {
        questionRepository.saveAll(questions)
    }

    fun getByCategoryType(categoryType: QuestionCategoryType, user: User): AiQuestionReadAllResponse =
        questionRepository.getQuestionByCategoryTypeAndUserId(categoryType, user)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_QUESTION)

    fun getByUserAndGroupId(user: User, groupId: UUID): PortfolioListReadResponse =
        questionRepository.getByUserAndGroupId(user, groupId)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_QUESTION)

    fun countByUser(user: User): Int {
        val u = requireUser(user)
        return questionRepository.countByAuthor(u)
    }

    fun findQuestionsByUserId(page: Int, userId: Long): QuestionPageResponse<QuestionResponse> {
        userService.getUser(userId) // 존재 확인

        val current = rq.getUser() // 이미 절대 non-null 로 설계되어 있음

        // 본인이거나 관리자만 가능
        if (current.id != userId && current.role != Role.ADMIN) {
            throw ErrorException(ErrorCode.QUESTION_INVALID_USER)
        }

        val pageNum = maxOf(page, 1)
        val pageable: Pageable = PageRequest.of(pageNum - 1, 15, Sort.by("createDate").descending())

        val questionsPage = questionRepository.findByAuthorId(userId, pageable)
        val responses = questionsPage.content.map { QuestionResponse.from(it) }

        return QuestionPageResponse.from(questionsPage, responses)
    }
}