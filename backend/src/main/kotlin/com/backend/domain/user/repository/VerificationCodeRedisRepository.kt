package com.backend.domain.user.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDateTime

@Repository
class VerificationCodeRedisRepository(
    private val redisTemplate: StringRedisTemplate
) {
    private val hashOps = redisTemplate.opsForHash<String, String>()

    fun save(email: String, code: String, ttlSeconds: Long) {
        val key = "verify:$email"

        hashOps.put(key, "email", email)
        hashOps.put(key, "code", code)
        hashOps.put(key, "verified", "false")
        hashOps.put(key, "expiresAt", LocalDateTime.now().plusMinutes(5).toString())

        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds))
    }

    fun findCode(email: String): String? {
        val key = "verify:$email"
        return hashOps[key, "code"]
    }

    fun delete(email: String) {
        redisTemplate.delete("verify:$email")
    }
}