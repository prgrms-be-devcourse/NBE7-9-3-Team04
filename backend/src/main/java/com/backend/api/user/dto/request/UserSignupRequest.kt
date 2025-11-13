package com.backend.api.user.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*

@JvmRecord
data class UserSignupRequest(

    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Schema(description = "사용자 이메일", example = "user@example.com")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*\\d)[a-z\\d]{5,30}$",
        message = "비밀번호는 5~30자, 영어 소문자와 숫자를 포함해야 합니다."
    )

    @field:Schema(description = "사용자 비밀번호", example = "abc12345")
    val password: String,

    @field:NotBlank(message = "이름은 필수입니다.")
    @field:Schema(description = "사용자 이름", example = "홍길동")
    val name: String,

    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Schema(description = "사용자 닉네임", example = "spring_dev")
    val nickname: String,

    @field:NotNull(message = "나이는 필수입니다.")
    @field:Min(value = 1, message = "나이는 1 이상입니다.")
    @field:Schema(description = "사용자 나이", example = "25")
    val age: Int,

    @field:NotBlank(message = "GitHub 주소는 필수입니다.")
    @field:Schema(description = "사용자 GitHub 프로필 URL", example = "https://github.com/user")
    val github: String,

    @field:Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    val image: String?
)
