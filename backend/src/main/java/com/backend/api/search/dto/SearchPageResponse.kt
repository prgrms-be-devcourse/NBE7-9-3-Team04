package com.backend.api.search.dto

import org.springframework.data.domain.Page

data class SearchPageResponse<T>(
    val content: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val last: Boolean
) {

    companion object {
        fun <T> from(page: Page<*>, content: List<T>): SearchPageResponse<T> =
            SearchPageResponse(
                content = content,
                currentPage = page.number + 1,
                totalPages = page.totalPages,
                totalElements = page.totalElements,
                last = page.isLast
            )
    }
}
