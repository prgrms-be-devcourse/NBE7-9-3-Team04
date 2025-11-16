package com.backend.api.search.service

import com.backend.api.search.dto.SearchPageResponse
import com.backend.api.search.dto.SearchResultDto
import com.backend.domain.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class GlobalSearchMySQLService(
    private val userRepository: UserRepository
) {

    fun searchAll(keyword: String, page: Int, size: Int): SearchPageResponse<SearchResultDto> {
        val pageNum = if (page < 1) 1 else page
        val pageable: Pageable = PageRequest.of(pageNum - 1, size, Sort.by(Sort.Direction.DESC, "id"))

        val like = "%$keyword%"

        val userPage = userRepository.searchByNameOrNickname(like, like, pageable)

        val content = userPage.content.map { u ->
            SearchResultDto(
                type = "user",
                id = u.id.toString(),
                title = u.name,
                snippet = u.nickname
            )
        }

        return SearchPageResponse.from(userPage, content)
    }
}
