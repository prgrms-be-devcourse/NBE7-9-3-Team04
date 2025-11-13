package com.backend.domain.subscription.repository

import com.backend.domain.subscription.entity.Subscription
import com.backend.domain.user.entity.User
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun existsByUserAndIsActiveTrue(user: User): Boolean
    fun findByCustomerKey(customerKey: String): Subscription?
    fun findByUser(user: User): Subscription?

    //fetch Join 사용
    @EntityGraph(attributePaths = ["user"])
    fun findByNextBillingDateAndIsActive(nextBillingDate: LocalDate, isActive: Boolean): List<Subscription>
}
