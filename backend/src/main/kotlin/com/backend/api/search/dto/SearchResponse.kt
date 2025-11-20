package com.backend.api.search.dto

data class SearchResponse<T>(
    val type: String,  // "post" | "user" | "question"
    val data: T
)