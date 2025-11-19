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
}