package com.backend.api.user.service

import com.backend.domain.user.entity.RefreshToken
import com.backend.domain.user.repository.RefreshRedisRepository
import org.springframework.stereotype.Service


@Service
class RefreshRedisService(
    private val refreshRedisRepository: RefreshRedisRepository
) {

    fun saveRefreshToken(userId: Long, token: String, ttlSeconds: Long) {
        val refreshToken: RefreshToken = RefreshToken(
            userId = userId,
            refreshToken = token,
            expiration = ttlSeconds
        )
        refreshRedisRepository.save(refreshToken)
    }

    fun getRefreshToken(userId: Long): String? =
        refreshRedisRepository.findByUserId(userId)

    fun deleteRefreshToken(userId: Long) =
        refreshRedisRepository.deleteByUserId(userId)

    fun existsRefreshToken(userId: Long?): Boolean =
        refreshRedisRepository.existsByUserId(userId)

}
