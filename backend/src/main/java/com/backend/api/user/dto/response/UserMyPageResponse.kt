package com.backend.api.user.dto.response

import com.backend.domain.user.entity.User
import java.time.LocalDateTime

class UserMyPageResponse(val userId: Long,
        val email: String,
        val name: String,
        val nickname: String,
        val age: Int,
        val github: String?,
        val image: String?) {

    class UserModify(val email: String?,
            val password: String?,
            val name: String?,
            val nickname: String?,
            val age: Int?,
            val github: String?,
            val image: String?)

    class SolvedProblem (
        val title: String,// 문제 제목
        val modifyDate: LocalDateTime?// 수정일
    )

    companion object {
        fun fromEntity(user: User): UserMyPageResponse =
            UserMyPageResponse(
                userId = user.id,
                email = user.email,
                name = user.name,
                nickname = user.nickname,
                age = user.age,
                github = user.github,
                image = user.image
            )
    }
}