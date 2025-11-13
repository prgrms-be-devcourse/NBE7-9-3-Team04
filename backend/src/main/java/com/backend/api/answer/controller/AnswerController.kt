package com.backend.api.answer.controller

import com.backend.api.answer.dto.request.AnswerCreateRequest
import com.backend.api.answer.dto.request.AnswerUpdateRequest
import com.backend.api.answer.dto.response.*
import com.backend.api.answer.service.AnswerService
import com.backend.domain.answer.entity.Answer
import com.backend.domain.user.entity.User
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/questions")
@Tag(name = "AnswerController", description = "면접 답변 API")
class AnswerController(
    private val rq: Rq,
    private val answerService: AnswerService
) {

    @PostMapping("/{questionId}/answers")
    @Operation(summary = "답변 작성")
    fun createAnswer(
        @PathVariable questionId: Long,
        @RequestBody @Valid reqBody: AnswerCreateRequest
    ): ApiResponse<AnswerCreateResponse> {
        val currentUser = rq.getUser()
        val newAnswer = answerService.writeAnswer(currentUser, questionId, reqBody)

        return ApiResponse.created(
            "${newAnswer.id}번 답변이 생성되었습니다.",
            AnswerCreateResponse.from(newAnswer)
        )
    }

    @PatchMapping("/{questionId}/answers/{answerId}")
    @Operation(summary = "답변 수정")
    fun updateAnswer(
        @PathVariable answerId: Long,
        @RequestBody @Valid reqBody: AnswerUpdateRequest
    ): ApiResponse<AnswerUpdateResponse> {
        val currentUser: User = rq.user
        val updatedAnswer: Answer = answerService.updateAnswer(currentUser, answerId, reqBody)

        return ApiResponse.ok(
            "${updatedAnswer.id}번 답변이 수정되었습니다.",
            AnswerUpdateResponse.from(updatedAnswer)
        )
    }


    @DeleteMapping("/{questionId}/answers/{answerId}")
    @Operation(summary = "답변 삭제")
    fun deleteAnswer(
        @PathVariable answerId: Long
    ): ApiResponse<Void?> {
        val currentUser: User = rq.user
        answerService.deleteAnswer(currentUser, answerId)

        return ApiResponse.ok(
            "${answerId}번 답변이 삭제되었습니다.",
            null
        )
    }


    @GetMapping("/{questionId}/answers")
    @Operation(summary = "답변 목록 조회")
    fun readAnswers(
        @PathVariable questionId: Long,
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<AnswerPageResponse<AnswerReadWithScoreResponse>> {
        val answersPage = answerService.findAnswersByQuestionId(page, questionId)

        return ApiResponse.ok(
            "${questionId}번 질문의 답변 목록 조회 성공",
            answersPage
        )
    }

    @GetMapping("/{questionId}/answers/mine")
    @Transactional(readOnly = true)
    @Operation(summary = "내 답변 조회")
    fun readMyAnswer(
        @PathVariable questionId: Long
    ): ApiResponse<AnswerReadResponse> {
        val answerResponse = answerService.findMyAnswer(questionId)

        return ApiResponse.ok(
            "${questionId}번 질문의 내 답변 조회 성공",
            answerResponse
        )
    }

}
