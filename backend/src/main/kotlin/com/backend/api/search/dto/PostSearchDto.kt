package com.backend.api.search.dto

data class PostSearchDto(
    val id: String,
    val title: String,
    val introduction: String,
    val content: String,
    val deadline: String,
    val recruitCount: Int,
    val postCategoryType: String,
    val authorNickname: String,
    val createdDate: String,
    val modifyDate: String
)
