package com.backend.api.search.service

import com.backend.api.post.service.PostSearchService
import com.backend.api.search.dto.PostSearchDto
import com.backend.api.search.dto.SearchPageResponse
import com.backend.api.search.dto.SearchResponse
import com.backend.api.search.dto.UserSearchDto
import com.backend.api.user.service.UserSearchService
import org.springframework.stereotype.Service

@Service
class GlobalSearchService(
    private val userSearchService: UserSearchService,
    private val postSearchService: PostSearchService
    // private val questionSearchService: QuestionSearchService
) {

    // 유저 검색 (Elasticsearch 기반)
    fun searchUser(keyword: String, page: Int, size: Int):SearchPageResponse<SearchResponse<UserSearchDto>> {
        val users = userSearchService.search(keyword, page, size)

        val content = users.content.map { u ->
            SearchResponse(
                type = "user",
                data = UserSearchDto(
                    id = u.id,
                    name = u.name,
                    nickname = u.nickname,
                    email = u.email,
                    role = u.role.toString()
                )
            )
        }

        return SearchPageResponse.from(users, content)
    }

    // Post 검색 (Elasticsearch 기반)
    fun searchPost(keyword: String, page: Int, size: Int): SearchPageResponse<SearchResponse<PostSearchDto>> {
        val posts = postSearchService.search(keyword, page, size)

        val content = posts.content.map { p ->
            SearchResponse(
                type = "post",
                data = PostSearchDto(
                    id = p.id,
                    title = p.title,
                    introduction = p.introduction,
                    content = p.content,
                    deadline = p.deadline,
                    recruitCount = p.recruitCount,
                    postCategoryType = p.postCategoryType.toString(),
                    authorNickname = p.authorNickname,
                    createdDate = p.createdDate,
                    modifyDate = p.modifyDate
                )
            )
        }

        return SearchPageResponse.from(posts, content)
    }


    // 통합 검색 (현재는 User만)
//    fun searchAll(keyword: String, page: Int, size: Int): SearchPageResponse<SearchResultDto> {
//        return searchUser(keyword, page, size)
//    }

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
