package com.backend.domain.payment.repository

import com.backend.domain.payment.entity.Payment
import com.backend.domain.payment.entity.PaymentStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: String): Payment?
    fun findByPaymentKey(paymentKey: String): Payment?

    fun findAllByOrderByApprovedAtDesc(): List<Payment>
    fun countByStatus(status: PaymentStatus): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId")
    fun findByOrderIdForUpdate(orderId: String): Payment?
}
