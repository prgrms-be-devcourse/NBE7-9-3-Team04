package com.backend.domain.payment.repository

import com.backend.domain.payment.entity.Payment
import com.backend.domain.payment.entity.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: String): Payment?
    fun findByPaymentKey(paymentKey: String): Payment?

    fun findAllByOrderByApprovedAtDesc(): List<Payment>
    fun countByStatus(status: PaymentStatus): Long
}
