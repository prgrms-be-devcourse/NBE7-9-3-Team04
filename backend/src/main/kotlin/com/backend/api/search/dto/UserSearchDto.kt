package com.backend.api.search.dto

data class UserSearchDto(
    val id: String,
    val name: String,
    val nickname: String,
    val email: String,
    val role: String
)
