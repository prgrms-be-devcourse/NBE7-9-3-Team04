package com.backend.api.user.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern


data class UserLoginRequest(

    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Schema(description = "사용자 이메일", example = "user@example.com")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*\\d)[a-z\\d]{5,30}$",
        message = "비밀번호는 5~30자, 영어 소문자와 숫자를 포함해야 합니다."
    )
    @field:Schema(description = "사용자 비밀번호", example = "abc12345")
    val password: String
)
