package com.backend.api.qna.controller

import com.backend.api.qna.dto.request.QnaAddRequest
import com.backend.api.qna.dto.request.QnaUpdateRequest
import com.backend.api.qna.dto.response.QnaPageResponse
import com.backend.api.qna.dto.response.QnaResponse
import com.backend.api.qna.service.QnaService
import com.backend.domain.qna.entity.QnaCategoryType
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/qna")
@Tag(name = "Qna", description = "Qna 관련 API")
class QnaController(
    private val qnaService: QnaService,
    private val rq: Rq
) {

    @PostMapping
    @Operation(summary = "Qna 생성", description = "사용자가 Qna를 생성합니다.")
    fun addQna(
        @Valid @RequestBody request: QnaAddRequest
    ): ApiResponse<QnaResponse> {
        val qnaResponse = qnaService.addQna(request, rq.getUser())
        return ApiResponse.created("Qna가 생성되었습니다.", qnaResponse)
    }

    @PutMapping("/{qnaId}")
    @Operation(summary = "Qna 수정", description = "사용자가 Qna를 수정합니다.")
    fun updateQna(
        @PathVariable qnaId: Long,
        @Valid @RequestBody request: QnaUpdateRequest
    ): ApiResponse<QnaResponse> {
        val qnaResponse = qnaService.updateQna(qnaId, request, rq.getUser())
        return ApiResponse.ok("Qna가 수정되었습니다.", qnaResponse)
    }

    @DeleteMapping("/{qnaId}")
    @Operation(summary = "Qna 삭제", description = "사용자가 Qna를 삭제합니다.")
    fun deleteQna(
        @PathVariable qnaId: Long
    ): ApiResponse<Void> {
        qnaService.deleteQna(qnaId, rq.getUser())
        return ApiResponse.ok("Qna가 삭제되었습니다.", null)
    }

    @GetMapping
    @Operation(summary = "Qna 전체 조회", description = "사용자가 모든 Qna를 조회합니다.")
    fun getAllQna(
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<QnaPageResponse<QnaResponse>> {
        val qnaPage = qnaService.getQnaAll(page, null)
        return ApiResponse.ok("Qna 목록 조회 성공", qnaPage)
    }

    @GetMapping("/{qnaId}")
    @Operation(summary = "Qna 단건 조회", description = "사용자가 특정 Qna를 조회합니다.")
    fun getMyQna(
        @PathVariable qnaId: Long
    ): ApiResponse<QnaResponse> {
        val response = qnaService.getQna(qnaId)
        return ApiResponse.ok("${qnaId}번 Qna 조회 성공", response)
    }

    @GetMapping("/category/{categoryType}")
    @Operation(summary = "카테고리별 Qna 조회", description = "사용자가 특정 카테고리의 Qna를 조회합니다.")
    fun getMyQnaByCategory(
        @PathVariable categoryType: QnaCategoryType,
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<QnaPageResponse<QnaResponse>> {
        val qnaPage = qnaService.getQnaAll(page, categoryType)
        return ApiResponse.ok("${categoryType.name} 카테고리 Qna 조회 성공", qnaPage)
    }
}
