package com.backend.global.security

import com.backend.domain.user.repository.UserRepository
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

//DB에서 사용자 정보 로드
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_EMAIL) }

        return CustomUserDetails(user)
    }
}
