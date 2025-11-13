package com.backend.domain.user.repository

import com.backend.domain.user.entity.VerificationCode
import org.springframework.data.jpa.repository.JpaRepository

interface VerificationCodeRepository : JpaRepository<VerificationCode, Long> {
    fun findByEmail(email: String): VerificationCode?
}
