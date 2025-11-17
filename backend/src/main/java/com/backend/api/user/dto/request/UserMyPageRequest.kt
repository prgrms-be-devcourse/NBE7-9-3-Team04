package com.backend.api.user.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class MyPageRequest(
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
    val image: String?
) {
    class UserModify(
        val email: String?,
        val password: String?,
        val name: String?,
        val nickname: String?,
        val age: Int?,
        val github: String?,
        val image: String?
    )
}
