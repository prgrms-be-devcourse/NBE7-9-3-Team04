package com.backend.api.payment.service

import com.backend.api.payment.dto.response.AdminPaymentResponse
import com.backend.api.payment.dto.response.AdminPaymentSummaryResponse
import com.backend.domain.payment.entity.PaymentStatus
import com.backend.domain.payment.repository.PaymentRepository
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class AdminPaymentService(
    private val paymentRepository: PaymentRepository
) {

    @Transactional(readOnly = true)
    fun getAllByOrderByApprovedAtDesc(): List<AdminPaymentResponse> {
        val payments = paymentRepository.findAllByOrderByApprovedAtDesc()

        if (payments.isEmpty()) {
            throw ErrorException(ErrorCode.PAYMENT_NOT_FOUND)
        }

        return payments.map { AdminPaymentResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getPaymentSummary(): AdminPaymentSummaryResponse {
        val total = paymentRepository.count()

        if (total == 0L) {
            throw ErrorException(ErrorCode.PAYMENT_NOT_FOUND)
        }

        val successCount = paymentRepository.countByStatus(PaymentStatus.DONE)

        val totalRevenue = paymentRepository.findAll()
            .filter { it.status == PaymentStatus.DONE }
            .sumOf { it.totalAmount }

        return AdminPaymentSummaryResponse(
            totalPayments = total,
            successPayments = successCount,
            totalRevenue = totalRevenue
        )
    }
}
