package com.backend.api.answer.service

import com.backend.api.answer.dto.request.AnswerCreateRequest
import com.backend.api.answer.dto.request.AnswerUpdateRequest
import com.backend.api.answer.dto.response.AnswerMypageResponse
import com.backend.api.answer.dto.response.AnswerPageResponse
import com.backend.api.answer.dto.response.AnswerReadResponse
import com.backend.api.answer.dto.response.AnswerReadWithScoreResponse
import com.backend.api.feedback.event.publisher.FeedbackPublisher
import com.backend.api.question.service.QuestionService
import com.backend.api.user.service.UserService
import com.backend.domain.answer.entity.Answer
import com.backend.domain.answer.repository.AnswerRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.global.Rq.Rq
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AnswerService(
    private val answerRepository: AnswerRepository,
    private val questionService: QuestionService,
    private val rq: Rq,
    private val userService: UserService,
    private val feedbackPublisher: FeedbackPublisher
) {

    fun findByIdOrThrow(id: Long): Answer {
        return answerRepository.findById(id)
            .orElseThrow { ErrorException(ErrorCode.ANSWER_NOT_FOUND) }
    }

    @Transactional
    fun writeAnswer(currentUser: User, questionId: Long, reqBody: AnswerCreateRequest): Answer {
        val content = reqBody.content!!

        val question = questionService.findByIdOrThrow(questionId)
        val isPublic = reqBody.isPublic!!

        val answer = Answer(
            content,
            isPublic,
            currentUser,
            question
        )
        val savedAnswer = answerRepository.save(answer)
        feedbackPublisher.publishFeedbackCreate(answer)

        return savedAnswer
    }

    @Transactional
    fun updateAnswer(currentUser: User, answerId: Long, reqBody: AnswerUpdateRequest): Answer {
        val answer = findByIdOrThrow(answerId)

        if (answer.author.id != currentUser.id) {
            throw ErrorException(ErrorCode.ANSWER_INVALID_USER)
        }

        answer.update(reqBody.content, reqBody.isPublic)
        feedbackPublisher.publishFeedbackUpdate(answer)
        return answer
    }

    @Transactional
    fun deleteAnswer(currentUser: User, answerId: Long) {
        val answer = findByIdOrThrow(answerId)

        if (answer.author.id != currentUser.id) {
            throw ErrorException(ErrorCode.ANSWER_INVALID_USER)
        }

        answerRepository.delete(answer)
    }

    fun findAnswersByQuestionId(pageArg: Int, questionId: Long): AnswerPageResponse<AnswerReadWithScoreResponse> {
        var page = pageArg
        questionService.findByIdOrThrow(questionId)

        if (page < 1) page = 1
        val pageable: Pageable = PageRequest.of(page - 1, 10)
        val answersPage =
            answerRepository.findByQuestionIdAndIsPublicTrueOrderByFeedbackScoreDesc(questionId, pageable)

        val answers = answersPage.content
            .map { answer: Answer ->
                val score = if (answer.feedback != null) answer.feedback!!.aiScore else 0
                AnswerReadWithScoreResponse.from(answer, score)
            }

        return AnswerPageResponse.from(answersPage, answers)
    }

    fun findMyAnswer(questionId: Long): AnswerReadResponse? {
        // 질문 존재 여부 확인
        questionService.findByIdOrThrow(questionId)

        val currentUser = rq.getUser()

        // 질문에 대해 현재 사용자가 작성한 답변 조회
        return answerRepository.findFirstByQuestionIdAndAuthorId(questionId, currentUser.id!!)
            .map { answer: Answer -> AnswerReadResponse.from(answer) } // 있으면 DTO로 변환
            .orElse(null) // 없으면 null 반환
    }

    fun findAnswersByUserId(pageArg: Int, userId: Long): AnswerPageResponse<AnswerMypageResponse> {
        var page = pageArg
        userService.getUser(userId)
        val currentUser = rq.getUser()
        if (currentUser.id != userId && currentUser.role != Role.ADMIN) {
            throw ErrorException(ErrorCode.ANSWER_INVALID_USER)
        }

        if (page < 1) page = 1
        val pageable: Pageable = PageRequest.of(page - 1, 15, Sort.by("createDate").descending())
        val answersPage = answerRepository.findByAuthorId(userId, pageable)

        val answers = answersPage
            .content
            .map { answer: Answer -> AnswerMypageResponse.from(answer) }

        return AnswerPageResponse.from(answersPage, answers)
    }
}
