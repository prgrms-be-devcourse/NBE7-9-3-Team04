package com.backend.api.user.dto.request

data class UserModifyRequest (
    val email: String?,
    val password: String?,
    val name: String?,
    val nickname: String?,
    val age: Int?,
    val github: String?,
    val image: String?
)