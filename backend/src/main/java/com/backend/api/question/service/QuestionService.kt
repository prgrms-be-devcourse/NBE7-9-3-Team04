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

    // 사용자 권한 검증
    private fun validateUserAuthority(user: User?) {
        if (user == null) throw ErrorException(ErrorCode.UNAUTHORIZED_USER)
        if (user.role != Role.USER) throw ErrorException(ErrorCode.FORBIDDEN)
    }

    // 질문 존재 여부 검증
    fun findByIdOrThrow(questionId: Long): Question =
        questionRepository.findById(questionId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_QUESTION) }

    // 질문 작성자 동일 여부 검증
    private fun validateQuestionAuthor(question: Question, user: User) {
        if (question.author.id != user.id) {
            throw ErrorException(ErrorCode.FORBIDDEN)
        }
    }

    private fun validateApprovedQuestion(question: Question) {
        if (question.isApproved != true) {
            throw ErrorException(ErrorCode.QUESTION_NOT_APPROVED)
        }
    }

    // 사용자 질문 생성
    @Transactional
    fun addQuestion(@Valid request: QuestionAddRequest, user: User?): QuestionResponse {
        validateUserAuthority(user)
        val question = createQuestion(request, user!!)
        val saved = saveQuestion(question)
        return QuestionResponse.from(saved)
    }

    private fun createQuestion(request: QuestionAddRequest, user: User): Question =
        Question(
            title = request.title,
            content = request.content,
            author = user,
            categoryType = request.categoryType
        )

    private fun saveQuestion(question: Question): Question =
        questionRepository.save(question)

    // 사용자 질문 수정
    @Transactional
    fun updateQuestion(questionId: Long, @Valid request: QuestionUpdateRequest, user: User?): QuestionResponse {
        validateUserAuthority(user)
        val question = findByIdOrThrow(questionId)
        validateQuestionAuthor(question, user!!)
        updateQuestionContent(question, request)
        return QuestionResponse.from(question)
    }

    private fun updateQuestionContent(question: Question, request: QuestionUpdateRequest) {
        question.updateUserQuestion(
            request.title,
            request.content,
            request.categoryType
        )
    }

    // 승인된 질문 전체 조회
    fun getApprovedQuestions(page: Int, categoryType: QuestionCategoryType?): QuestionPageResponse<QuestionResponse> {
        var pageNum = page
        if (pageNum < 1) pageNum = 1

        val pageable: Pageable = PageRequest.of(pageNum - 1, 9, Sort.by("createDate").descending())

        val questionsPage: Page<Question> = when {
            categoryType == null ->
                questionRepository.findApprovedQuestionsExcludingCategory(QuestionCategoryType.PORTFOLIO, pageable)

            categoryType == QuestionCategoryType.PORTFOLIO ->
                questionRepository.findApprovedQuestionsByCategory(QuestionCategoryType.PORTFOLIO, pageable)

            else ->
                questionRepository.findByCategoryTypeAndIsApprovedTrue(categoryType, pageable)
        }

        if (questionsPage.isEmpty) throw ErrorException(ErrorCode.NOT_FOUND_QUESTION)

        val questions = mapToResponseList(questionsPage)

        return QuestionPageResponse.from(questionsPage, questions)
    }

    private fun mapToResponseList(page: Page<Question>): List<QuestionResponse> =
        page.content.map { QuestionResponse.from(it) }

    // 승인된 질문 단건 조회
    fun getApprovedQuestionById(questionId: Long): QuestionResponse {
        val question = findByIdOrThrow(questionId)
        validateApprovedQuestion(question)
        return QuestionResponse.from(question)
    }

    // 승인되지 않은 질문 단건 조회 (수정용)
    fun getNotApprovedQuestionById(userId: Long, questionId: Long, user: User?): QuestionResponse {
        validateUserAuthority(user)

        if (user!!.id != userId) {
            throw ErrorException(ErrorCode.QUESTION_INVALID_USER)
        }

        val question = findByIdOrThrow(questionId)
        validateQuestionAuthor(question, user)

        if (question.isApproved == true) {
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

    // 사용자 작성 질문 수
    fun countByUser(user: User?): Int {
        validateUserAuthority(user)
        return questionRepository.countByAuthor(user!!)
    }

    // 사용자 아이디로 질문 조회
    fun findQuestionsByUserId(page: Int, userId: Long): QuestionPageResponse<QuestionResponse> {
        userService.getUser(userId)
        val currentUser = rq.getUser()

        if (currentUser.id != userId && currentUser.role != Role.ADMIN) {
            throw ErrorException(ErrorCode.QUESTION_INVALID_USER)
        }

        var pageNum = page
        if (pageNum < 1) pageNum = 1

        val pageable: Pageable = PageRequest.of(pageNum - 1, 15, Sort.by("createDate").descending())
        val questionsPage = questionRepository.findByAuthorId(userId, pageable)

        val list = questionsPage.content.map { QuestionResponse.from(it) }

        return QuestionPageResponse.from(questionsPage, list)
    }
}
