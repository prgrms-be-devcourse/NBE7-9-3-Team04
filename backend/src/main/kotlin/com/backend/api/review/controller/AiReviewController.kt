package com.backend.api.review.controller

import com.backend.api.review.dto.response.AiReviewResponse
import com.backend.api.review.service.AiReviewService
import com.backend.domain.user.entity.User
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/portfolio-review")
@Tag(name = "Portfolio Ai Feedback", description = "포트폴리오 AI 첨삭")
class AiReviewController(
    private val aiReviewService: AiReviewService,
    private val rq: Rq
) {

    private val currentUser: User
        get() = rq.getUser()

    @PostMapping
    @Operation(summary = "포트폴리오 AI 첨삭 생성", description = "사용자의 포트폴리오를 바탕으로 AI 첨삭을 생성합니다.")
    fun createResumeFeedback(): ApiResponse<AiReviewResponse> {
        val user = currentUser
        val response = aiReviewService.createAiReview(user)
        return ApiResponse.created("포트폴리오 AI 첨삭이 완료되었습니다.", response)
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "AI 첨삭 단건 조회", description = "ID를 사용하여 특정 AI 첨삭 내용을 조회합니다.")

    fun getReviewById(@PathVariable(required = false) reviewId: String?): ApiResponse<AiReviewResponse> {
        try {

            if (reviewId == null || reviewId == "undefined") {
                throw ErrorException(ErrorCode.INVALID_PARAMETER)
            }

            val validReviewId = reviewId.toLong()
            val user = currentUser
            val response = aiReviewService.findReviewById(validReviewId, user)
            return ApiResponse.ok("AI 첨삭 조회가 완료되었습니다.", response)
        } catch (e: NumberFormatException) {
            throw ErrorException(ErrorCode.INVALID_PARAMETER)
        }
    }


    @Operation(
        summary = "AI 첨삭 다건 조회",
        description = "로그인한 사용자의 모든 AI 첨삭 목록을 최신순으로 조회합니다."
    )
    @GetMapping("/reviews")
    fun getMyReviews(): ApiResponse<List<AiReviewResponse>> {
        val user = currentUser
        val responses = aiReviewService.findMyAiReviews(user)
        return ApiResponse.ok("내 AI 첨삭 목록 조회가 완료되었습니다.", responses)
    }
}