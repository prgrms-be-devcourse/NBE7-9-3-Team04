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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminQuestionService(
    private val questionRepository: QuestionRepository,
    private val userService: UserService
) {

    // 관리자 권한 검증
    private fun requireAdmin(user: User): User {
        if (user.role != Role.ADMIN) throw ErrorException(ErrorCode.FORBIDDEN)
        return user
    }

    // ID로 질문 조회
    private fun findByIdOrThrow(questionId: Long): Question =
        questionRepository.findByIdOrNull(questionId)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_QUESTION)

    // 관리자 질문 생성
    @Transactional
    fun addQuestion(@Valid request: AdminQuestionAddRequest, user: User): QuestionResponse {
        val admin = requireAdmin(user)

        val question = Question(
            title = request.title,
            content = request.content,
            author = admin,
            categoryType = request.categoryType
        ).apply {
            request.isApproved?.let { updateApproved(it) }
            request.score?.let { updateScore(it) }
        }

        val saved = questionRepository.save(question)
        return QuestionResponse.from(saved)
    }

    // 관리자 질문 수정
    @Transactional
    fun updateQuestion(
        questionId: Long,
        @Valid request: AdminQuestionUpdateRequest,
        user: User
    ): QuestionResponse {
        requireAdmin(user)

        val question = findByIdOrThrow(questionId)

        question.updateAdminQuestion(
            request.title,
            request.content,
            request.isApproved,
            request.score,
            request.categoryType
        )

        return QuestionResponse.from(question)
    }

    // 승인/비승인 처리
    @Transactional
    fun approveQuestion(questionId: Long, isApproved: Boolean, user: User): QuestionResponse {
        requireAdmin(user)

        val question = findByIdOrThrow(questionId)
        question.updateApproved(isApproved)

        return QuestionResponse.from(question)
    }

    // 점수 수정
    @Transactional
    fun setQuestionScore(questionId: Long, score: Int?, user: User): QuestionResponse {
        requireAdmin(user)

        val question = findByIdOrThrow(questionId)
        score?.let { question.updateScore(it) }

        return QuestionResponse.from(question)
    }

    // 관리자 질문 전체 조회
    fun getAllQuestions(page: Int, user: User): QuestionPageResponse<QuestionResponse> {
        requireAdmin(user)

        val pageNum = maxOf(page, 1)
        val pageable: Pageable = PageRequest.of(pageNum - 1, 15, Sort.by("createDate").descending())

        val questionsPage = questionRepository.findAll(pageable)

        if (questionsPage.isEmpty) throw ErrorException(ErrorCode.NOT_FOUND_QUESTION)

        val responses = questionsPage.content.map { QuestionResponse.from(it) }

        return QuestionPageResponse.from(questionsPage, responses)
    }

    // 관리자 질문 단건 조회
    fun getQuestionById(questionId: Long, user: User): QuestionResponse {
        requireAdmin(user)

        val question = findByIdOrThrow(questionId)
        return QuestionResponse.from(question)
    }

    // 관리자 질문 삭제
    @Transactional
    fun deleteQuestion(questionId: Long, user: User) {
        requireAdmin(user)

        val question = findByIdOrThrow(questionId)
        questionRepository.delete(question)
    }
}