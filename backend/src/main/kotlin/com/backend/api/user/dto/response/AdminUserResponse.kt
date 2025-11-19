package com.backend.api.user.dto.response

import com.backend.domain.user.entity.AccountStatus
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자용 사용자 조회 응답 DTO")
data class AdminUserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val nickname: String,
    val age: Int,
    val github: String,
    val image: String?,
    val role: Role,
    val accountStatus: AccountStatus
) {

    companion object {
        fun from(user: User): AdminUserResponse {
            return AdminUserResponse(
                user.id,
                user.email,
                user.name,
                user.nickname,
                user.age,
                user.github,
                user.image,
                user.role,
                user.accountStatus
            )
        }
    }
}