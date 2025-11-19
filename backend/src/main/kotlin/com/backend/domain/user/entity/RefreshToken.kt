package com.backend.domain.user.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@Entity
@RedisHash(value = "refreshToken")
class RefreshToken(

    @Id
    val userId: Long,

    var refreshToken: String,

    @TimeToLive
    var expiration: Long

)
