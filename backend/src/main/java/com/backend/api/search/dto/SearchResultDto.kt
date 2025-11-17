package com.backend.api.search.dto

data class SearchResultDto(
    val type: String,     // "user", "post", "question"
    val id: String,
    val title: String,
    val snippet: String
)
