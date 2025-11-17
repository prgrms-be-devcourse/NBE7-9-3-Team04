package com.backend.domain.userPenalty.entity

import com.backend.domain.user.entity.AccountStatus
import com.backend.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "userPenalty")
class UserPenalty(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    val user: User,

    @Column(nullable = false, length = 255)
    val reason: String?,

    @Column(nullable = false)
    val startAt: LocalDateTime,

    val endAt: LocalDateTime? = null,

    @Column(nullable = false)
    var released: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val appliedStatus: AccountStatus

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun isExpired(): Boolean {
        val end = endAt
        return !released && end != null && end.isBefore(LocalDateTime.now())
    }

    fun markReleased() {
        released = true
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var user: User = User() // Java builder 호출 시 null 발생 방지
        private var reason: String = ""
        private var startAt: LocalDateTime = LocalDateTime.now()
        private var endAt: LocalDateTime? = null
        private var released: Boolean = false
        private var appliedStatus: AccountStatus = AccountStatus.SUSPENDED

        fun user(user: User) = apply { this.user = user }
        fun reason(reason: String) = apply { this.reason = reason }
        fun startAt(startAt: LocalDateTime) = apply { this.startAt = startAt }
        fun endAt(endAt: LocalDateTime?) = apply { this.endAt = endAt }
        fun released(released: Boolean) = apply { this.released = released }
        fun appliedStatus(status: AccountStatus) = apply { this.appliedStatus = status }

        fun build() = UserPenalty(
            user = user,
            reason = reason,
            startAt = startAt,
            endAt = endAt,
            released = released,
            appliedStatus = appliedStatus
        )
    }
}