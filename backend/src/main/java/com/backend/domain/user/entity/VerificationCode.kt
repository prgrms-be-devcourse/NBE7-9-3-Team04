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

    // ---------- Kotlin Builder ----------
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var email: String = ""
        private var code: String = ""
        private var expiresAt: LocalDateTime = LocalDateTime.now()
        private var verified: Boolean = false

        fun email(email: String) = apply { this.email = email }
        fun code(code: String) = apply { this.code = code }
        fun expiresAt(expiresAt: LocalDateTime) = apply { this.expiresAt = expiresAt }
        fun verified(verified: Boolean) = apply { this.verified = verified }

        fun build(): VerificationCode {
            return VerificationCode(
                email = email,
                code = code,
                expiresAt = expiresAt,
                verified = verified
            )
        }
    }
}
