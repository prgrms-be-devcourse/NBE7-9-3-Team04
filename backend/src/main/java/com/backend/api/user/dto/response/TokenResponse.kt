package com.backend.api.user.dto.response


data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

