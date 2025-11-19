package com.backend.domain.user.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class VerificationCode(

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var code: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    var verified: Boolean = false

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun isExpired(): Boolean = expiresAt.isBefore(LocalDateTime.now())

    fun markAsVerified() {
        verified = true
    }
}
