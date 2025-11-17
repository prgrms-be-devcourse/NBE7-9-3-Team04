package com.backend.api.billing.controller

import com.backend.api.billing.dto.request.BillingRequest
import com.backend.api.billing.dto.response.BillingResponse
import com.backend.api.billing.service.BillingService
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/billing")
@Tag(name = "Billing", description = "자동결제 관련 API")
class BillingController(
    private val billingService: BillingService
) {

    @PostMapping("/confirm")
    @Operation(summary = "토스 빌링키 발급", description = "authKey와 customerKey를 이용해 토스 빌링키를 발급받고 구독 상태를 업데이트합니다.")
    fun issueBillingKey(@RequestBody request: BillingRequest): ApiResponse<BillingResponse> {

        val response = billingService.issueBillingKey(request)
        return ApiResponse.ok("빌링키가 발급되었습니다.", response)
    }
}
