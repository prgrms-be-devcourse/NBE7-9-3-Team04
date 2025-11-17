package com.backend.api.user.dto.response

import com.backend.domain.user.entity.User
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class UserLoginResponse(
    @field:Schema(description = "사용자 ID", example = "1")
    val id: Long,

    @field:Schema(description = "사용자 이메일", example = "user@example.com")
     val email: String,

    @field:Schema(description = "사용자 이름", example = "홍길동")
    val name: String,

    @field:Schema(description = "사용자 닉네임", example = "spring_dev")
    val nickname: String,

    @field:Schema(description = "사용자 나이", example = "25")
    val age: Int,

    @field:Schema(description = "사용자 GitHub 프로필 URL", example = "https://github.com/user")
    val github: String,

    @field:Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    val image: String?,

    @field:Schema(description = "사용자 권한", example = "USER")
    val role: String,

    @field:Schema(description = "생성일", example = "2025-10-13T10:15:30")
    val createDate: LocalDateTime,

    @field:Schema(description = "수정일", example = "2025-10-13T10:20:00")
    val modifyDate: LocalDateTime,

    @field:Schema(description = "accessToken", example = "accessToken")
    val accessToken: String?,
    @field:Schema(description = "refreshToken", example = "refreshToken")
    val refreshToken: String?


) {
    companion object {
        fun from(user: User, accessToken: String, refreshToken: String): UserLoginResponse {
            return UserLoginResponse(
                user.id,
                user.email,
                user.name,
                user.nickname,
                user.age,
                user.github,
                user.image,
                user.role.name,
                user.createDate,
                user.modifyDate,
                accessToken,
                refreshToken
            )
        }

        fun from(user: User): UserLoginResponse {
            return UserLoginResponse(
                user.id,
                user.email,
                user.name,
                user.nickname,
                user.age,
                user.github,
                user.image,
                user.role.name,
                user.createDate,
                user.modifyDate,
                null,
                null
            )
        }
    }
}
