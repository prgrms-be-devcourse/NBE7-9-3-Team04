package com.backend.api.question.controller

import com.backend.api.question.dto.request.*
import com.backend.api.question.dto.response.QuestionPageResponse
import com.backend.api.question.dto.response.QuestionResponse
import com.backend.api.question.service.AdminQuestionService
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/questions")
@Tag(name = "Admin Questions", description = "관리자 질문 관리 API")
class AdminQuestionController(
    private val adminQuestionService: AdminQuestionService,
    private val rq: Rq
) {

    @PostMapping
    @Operation(summary = "질문 생성 (관리자)", description = "관리자가 새로운 질문을 직접 등록합니다.")
    fun addQuestion(
        @Valid @RequestBody request: AdminQuestionAddRequest
    ): ApiResponse<QuestionResponse> {
        val response = adminQuestionService.addQuestion(request, rq.getUser())
        return ApiResponse.ok("질문이 생성되었습니다.", response)
    }

    @PutMapping("/{questionId}")
    @Operation(summary = "질문 수정 (관리자)", description = "관리자가 기존 질문을 수정합니다.")
    fun updateQuestion(
        @PathVariable questionId: Long,
        @Valid @RequestBody request: AdminQuestionUpdateRequest
    ): ApiResponse<QuestionResponse> {
        val response = adminQuestionService.updateQuestion(questionId, request, rq.getUser())
        return ApiResponse.ok("질문이 수정되었습니다.", response)
    }

    @PatchMapping("/{questionId}/approve")
    @Operation(summary = "질문 승인/비승인 처리 (관리자)", description = "관리자가 질문의 승인 상태를 변경합니다.")
    fun approveQuestion(
        @PathVariable questionId: Long,
        @Valid @RequestBody request: QuestionApproveRequest
    ): ApiResponse<QuestionResponse> {
        val response = adminQuestionService.approveQuestion(questionId, request.isApproved, rq.getUser())

        val message = if (request.isApproved)
            "질문이 승인 처리되었습니다."
        else
            "질문이 비승인 처리되었습니다."

        return ApiResponse.ok(message, response)
    }

    @PatchMapping("/{questionId}/score")
    @Operation(summary = "질문 점수 수정 (관리자)", description = "관리자가 질문의 점수를 수정합니다.")
    fun setQuestionScore(
        @PathVariable questionId: Long,
        @Valid @RequestBody request: QuestionScoreRequest
    ): ApiResponse<QuestionResponse> {
        val response = adminQuestionService.setQuestionScore(questionId, request.score, rq.getUser())
        return ApiResponse.ok("질문 점수가 수정되었습니다.", response)
    }

    @GetMapping
    @Operation(summary = "질문 전체 조회 (관리자)", description = "관리자가 질문 전체 조회합니다.(미승인 포함)")
    fun getAllQuestions(
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<QuestionPageResponse<QuestionResponse>> {
        val questionsPage = adminQuestionService.getAllQuestions(page, rq.getUser())
        return ApiResponse.ok("관리자 질문 목록 조회 성공", questionsPage)
    }

    @GetMapping("/{questionId}")
    @Operation(summary = "질문 단건 조회 (관리자)", description = "관리자가 질문 ID로 단건 조회합니다.(미승인 포함)")
    fun getQuestionById(
        @PathVariable questionId: Long
    ): ApiResponse<QuestionResponse> {
        val response = adminQuestionService.getQuestionById(questionId, rq.getUser())
        return ApiResponse.ok("관리자 질문 단건 조회 성공", response)
    }

    @DeleteMapping("/{questionId}")
    @Operation(summary = "질문 삭제 (관리자)", description = "관리자가 질문을 삭제합니다.")
    fun deleteQuestion(
        @PathVariable questionId: Long
    ): ApiResponse<Void> {
        adminQuestionService.deleteQuestion(questionId, rq.getUser())
        return ApiResponse.ok("관리자 질문 삭제 성공", null)
    }
}
