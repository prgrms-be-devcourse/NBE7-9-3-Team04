package com.backend.api.payment.controller

import com.backend.api.payment.dto.request.PaymentRequest
import com.backend.api.payment.dto.response.PaymentResponse
import com.backend.api.payment.service.PaymentService
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "결제 관련 API")
class PaymentController(
    private val paymentService: PaymentService
) {

    //결제 승인
    @PostMapping("/confirm")
    @Operation(summary = "결제 승인")
    fun confirm(
        @RequestBody request: PaymentRequest
    ): ApiResponse<PaymentResponse> {
        val response = paymentService.confirmPayment(request)
        return ApiResponse.created("결제가 승인되었습니다.", response)
    }

    @GetMapping("/key/{paymentKey}")
    @Operation(summary = "paymentKey로 결제 조회")
    fun geyPaymentByKey(
        @PathVariable paymentKey: String
    ): ApiResponse<PaymentResponse> {
        val response = paymentService.geyPaymentByKey(paymentKey)
        return ApiResponse.ok("결제 정보를 조회합니다", response)
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "orderId로 결제 조회")
    fun geyPaymentByOrderId(
        @PathVariable orderId: String
    ): ApiResponse<PaymentResponse> {
        val response = paymentService.getPaymentByOrderId(orderId)
        return ApiResponse.ok("결제 정보를 조회합니다", response)
    }
}
