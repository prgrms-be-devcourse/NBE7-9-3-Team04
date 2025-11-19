package com.backend.domain.subscription.repository

import com.backend.domain.subscription.entity.Subscription
import com.backend.domain.user.entity.User
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun existsByUserAndActiveTrue(user: User): Boolean
    fun findByCustomerKey(customerKey: String): Subscription?
    fun findByUser(user: User): Subscription?

    //fetch Join 사용
    @EntityGraph(attributePaths = ["user"])
    fun findByNextBillingDateAndActive(nextBillingDate: LocalDate, active: Boolean): List<Subscription>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.customerKey = :customerKey")
    fun findByCustomerKeyForUpdate(customerKey: String): Subscription?
}
