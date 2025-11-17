package com.backend.api.search.controller

import com.backend.api.question.service.QuestionSearchService
import com.backend.api.search.dto.SearchPageResponse
import com.backend.api.search.dto.SearchResultDto
import com.backend.api.search.service.GlobalSearchMySQLService
import com.backend.api.search.service.GlobalSearchService
import com.backend.global.dto.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/search")
class GlobalSearchController(
    private val globalSearchService: GlobalSearchService,
    private val userMySQLSearchService: GlobalSearchMySQLService,
    private val questionSearchService: QuestionSearchService
) {

    // ES 기반 통합 검색 (현재는 User만 지원)
    @GetMapping("/es")
    fun searchByES(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<SearchPageResponse<SearchResultDto>> {
        return ApiResponse.ok(globalSearchService.searchUser(keyword, page, size))
    }

    // MySQL 기반 통합 검색 (성능 비교용)
    @GetMapping("/mysql")
    fun searchByMySQL(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<SearchPageResponse<SearchResultDto>> {
        return ApiResponse.ok(userMySQLSearchService.searchAll(keyword, page, size))
    }

    // Post 검색 (추후 활성화)
    // @GetMapping("/posts/es")
    // fun searchPostByES(@RequestParam keyword: String): ApiResponse<List<SearchResultDto>> {
    //     return ApiResponse.ok(postSearchService.postSearch(keyword))
    // }

    // CS Question 검색 (추후 활성화)
    // @GetMapping("/questions/es")
    // fun searchQuestionByES(@RequestParam keyword: String): ApiResponse<List<SearchResultDto>> {
    //     return ApiResponse.ok(questionSearchService.questionSearch(keyword))
    // }
}
