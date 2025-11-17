package com.backend.api.payment.controller

import com.backend.api.payment.dto.response.AdminPaymentResponse
import com.backend.api.payment.dto.response.AdminPaymentSummaryResponse
import com.backend.api.payment.service.AdminPaymentService
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/payments")
@Tag(name = "Admin Payment", description = "결제 관리 API(관리자)")
class AdminPaymentController(
    private val adminPaymentService: AdminPaymentService
) {
    @GetMapping
    @Operation(summary = "전체 결제 내역 조회", description = "관리자가 모든 결제 내역을 조회합니다.")
    fun getAdminPayment(): ApiResponse<List<AdminPaymentResponse>> {
        val payments = adminPaymentService.getAllByOrderByApprovedAtDesc()
        return ApiResponse.ok("결제 내역을 조회합니다.", payments)
    }

    @GetMapping("/summary")
    @Operation(summary = "결제 통계 요약 조회", description = "총 결제 수, 성공 결제 수, 총 수익을 조회합니다.")
    fun getPaymentSummary(): ApiResponse<AdminPaymentSummaryResponse> {
        val summary = adminPaymentService.getPaymentSummary()
        return ApiResponse.ok("결제 요약 정보를 조회합니다.", summary)
    }
}