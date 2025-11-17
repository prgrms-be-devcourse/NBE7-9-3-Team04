package com.backend.api.search.service

import com.backend.api.search.dto.SearchPageResponse
import com.backend.api.search.dto.SearchResultDto
import com.backend.api.user.service.UserSearchService
import com.backend.domain.user.entity.search.UserDocument
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class GlobalSearchService(
    private val userSearchService: UserSearchService
    // private val postSearchService: PostSearchService
    // private val questionSearchService: QuestionSearchService
) {

    // 유저 검색 (Elasticsearch 기반)
    fun searchUser(keyword: String, page: Int, size: Int): SearchPageResponse<SearchResultDto> {
        val users: Page<UserDocument> = userSearchService.search(keyword, page, size)

        val content = users.content.map { u ->
            SearchResultDto(
                type = "user",
                id = u.id ?: "",
                title = u.name ?: "",
                snippet = u.nickname ?: ""
            )
        }

        return SearchPageResponse.from(users, content)
    }

    // 통합 검색 (현재는 User만)
    fun searchAll(keyword: String, page: Int, size: Int): SearchPageResponse<SearchResultDto> {
        return searchUser(keyword, page, size)
    }

    /*
    fun searchPost(keyword: String, page: Int, size: Int): SearchPageResponse<SearchResultDto> {
        val posts = postSearchService.search(keyword, page, size)
        val content = posts.content.map { p ->
            SearchResultDto(
                type = "post",
                id = p.id,
                title = p.title,
                snippet = p.content
            )
        }
        return SearchPageResponse.from(posts, content)
    }

    fun searchQuestion(keyword: String, page: Int, size: Int): SearchPageResponse<SearchResultDto> {
        val questions = questionSearchService.search(keyword, page, size)
        val content = questions.content.map { q ->
            SearchResultDto(
                type = "question",
                id = q.id,
                title = q.title,
                snippet = q.body
            )
        }
        return SearchPageResponse.from(questions, content)
    }
    */
}
