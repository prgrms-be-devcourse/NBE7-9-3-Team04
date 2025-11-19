package com.backend.api.user.dto.response

import com.backend.domain.user.entity.User
import io.swagger.v3.oas.annotations.media.Schema

data class UserMyPageResponse(
    @field:Schema(description = "유저 아이디", example = "5")
    val userId: Long,

    @field:Schema(description = "유저 이메일", example = "user@gmail.com")
    val email: String,

    @field:Schema(description = "유저 이름", example = "김아무개")
    val name: String,

    @field:Schema(description = "유저 닉네임", example = "김씨")
    val nickname: String,

    @field:Schema(description = "유저 나이", example = "25")
    val age: Int,

    @field:Schema(description = "유저 깃허브 주소", example = "github3")
    val github: String?,

    @field:Schema(description = "이미지 주소", example = "image3")
    val image: String?,

    @field:Schema(description = "OAuth 인증 시 받아오는 SNS의 고유 숫자 ID", example = "image3")
    val oauthId: String?
) {
    companion object {
        fun fromEntity(user: User): UserMyPageResponse {
            return UserMyPageResponse(
                userId = user.id,
                email = user.email,
                name = user.name,
                nickname = user.nickname,
                age = user.age,
                github = user.github,
                image = user.image,
                oauthId = user.oauthId
            )
        }
    }
}