package com.backend.api.question.service

import com.backend.api.question.dto.request.AdminQuestionAddRequest
import com.backend.api.question.dto.request.AdminQuestionUpdateRequest
import com.backend.api.question.dto.response.QuestionPageResponse
import com.backend.api.question.dto.response.QuestionResponse
import com.backend.api.user.service.UserService
import com.backend.domain.question.entity.Question
import com.backend.domain.question.repository.QuestionRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminQuestionService(
    private val questionRepository: QuestionRepository,
    private val userService: UserService
) {

    // 관리자 권한 검증
    private fun validateAdminAuthority(user: User?) {
        if (user == null) throw ErrorException(ErrorCode.UNAUTHORIZED_USER)
        if (user.role != Role.ADMIN) throw ErrorException(ErrorCode.FORBIDDEN)
    }

    // 질문 조회 또는 예외
    private fun findByIdOrThrow(questionId: Long): Question =
        questionRepository.findById(questionId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_QUESTION) }

    // 관리자 질문 생성
    @Transactional
    fun addQuestion(@Valid request: AdminQuestionAddRequest, user: User?): QuestionResponse {
        validateAdminAuthority(user)
        val question = createQuestion(request, user!!)
        val saved = questionRepository.save(question)
        return QuestionResponse.from(saved)
    }

    private fun createQuestion(request: AdminQuestionAddRequest, user: User): Question {
        val question = Question(
            title = request.title,
            content = request.content,
            author = user,
            categoryType = request.categoryType
        )

        request.isApproved?.let { question.updateApproved(it) }
        request.score?.let { question.updateScore(it) }

        return question
    }

    // 관리자 질문 수정
    @Transactional
    fun updateQuestion(questionId: Long, @Valid request: AdminQuestionUpdateRequest, user: User?): QuestionResponse {
        validateAdminAuthority(user)
        val question = findByIdOrThrow(questionId)
        updateAdminQuestion(question, request)
        return QuestionResponse.from(question)
    }

    private fun updateAdminQuestion(question: Question, request: AdminQuestionUpdateRequest) {
        question.updateAdminQuestion(
            request.title,
            request.content,
            request.isApproved,
            request.score,
            request.categoryType
        )
    }

    // 질문 승인/비승인 처리
    @Transactional
    fun approveQuestion(questionId: Long, isApproved: Boolean, user: User?): QuestionResponse {
        validateAdminAuthority(user)
        val question = findByIdOrThrow(questionId)
        question.updateApproved(isApproved)
        return QuestionResponse.from(question)
    }

    // 질문 점수 수정
    @Transactional
    fun setQuestionScore(questionId: Long, score: Int?, user: User?): QuestionResponse {
        validateAdminAuthority(user)
        val question = findByIdOrThrow(questionId)
        if (score != null) question.updateScore(score)
        return QuestionResponse.from(question)
    }

    // 관리자 질문 전체 조회
    fun getAllQuestions(page: Int, user: User?): QuestionPageResponse<QuestionResponse> {
        validateAdminAuthority(user)

        val pageNum = if (page < 1) 1 else page
        val pageable: Pageable = PageRequest.of(pageNum - 1, 15, Sort.by("createDate").descending())

        val questionsPage = questionRepository.findAll(pageable)

        if (questionsPage.isEmpty) throw ErrorException(ErrorCode.NOT_FOUND_QUESTION)

        val questions = questionsPage.content.map { QuestionResponse.from(it) }

        return QuestionPageResponse.from(questionsPage, questions)
    }

    // 관리자 질문 단건 조회
    fun getQuestionById(questionId: Long, user: User?): QuestionResponse {
        validateAdminAuthority(user)
        val question = findByIdOrThrow(questionId)
        return QuestionResponse.from(question)
    }

    // 관리자 질문 삭제
    @Transactional
    fun deleteQuestion(questionId: Long, user: User?) {
        validateAdminAuthority(user)
        val question = findByIdOrThrow(questionId)
        questionRepository.delete(question)
    }
}
