package com.backend.api.user.dto.response

import com.backend.domain.ranking.entity.Ranking
import com.backend.domain.ranking.entity.Tier
import com.backend.domain.subscription.entity.SubscriptionType
import com.backend.domain.user.entity.User
import io.swagger.v3.oas.annotations.media.Schema

@JvmRecord
data class UserSignupResponse(
    @field:Schema(description = "사용자 ID", example = "1")
     val id: Long,

    @field:Schema(description = "사용자 이메일", example = "user@example.com")
    val email: String,

    @field:Schema(description = "사용자 이름", example = "홍길동")
    val name: String,

    @field:Schema(description = "사용자 닉네임", example = "spring_dev")
    val nickname: String,

    @field:Schema(description = "사용자 구독 유형", example = "BASIC")
    val subscriptionType: SubscriptionType,

    @field:Schema(description = "사용자 티어", example = "UNRATED")
    val tier: Tier

) {
    companion object {
        fun from(user: User, ranking: Ranking): UserSignupResponse {
            return UserSignupResponse(
                user.id,
                user.email,
                user.name,
                user.nickname,
                user.subscription!!.subscriptionType,
                ranking.tier
            )
        }
    }
}