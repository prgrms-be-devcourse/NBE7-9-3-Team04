package com.backend.api.post.controller

import com.backend.api.post.dto.request.AdminPostPinRequest
import com.backend.api.post.dto.request.AdminPostStatusRequest
import com.backend.api.post.dto.response.PostPageResponse
import com.backend.api.post.dto.response.PostResponse
import com.backend.api.post.service.AdminPostService
import com.backend.domain.user.entity.User
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/posts")
@Tag(name = "Admin Post", description = "게시글 관리 API(관리자)")

class AdminPostController(
    private val adminPostService: AdminPostService,
    private val rq: Rq
) {

    private val currentUser: User
        get() = rq.getUser()

    @GetMapping
    @Operation(summary = "전체 게시글 조회", description = "관리자가 모든 게시글을 조회합니다.")
    fun getAllPosts(
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<PostPageResponse<PostResponse>> {
        val postsPage = adminPostService.getAllPosts(page, currentUser)
        return ApiResponse.ok("전체 게시글 조회 성공", postsPage)
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 단건 조회", description = "관리자가 특정 게시글을 조회합니다.")
    fun getPostById(
        @PathVariable postId: Long
    ): ApiResponse<PostResponse> {
        val response = adminPostService.getPostById(postId, currentUser)
        return ApiResponse.ok("게시글 단건 조회 성공", response)
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "관리자가 특정 게시글을 삭제합니다.")
    fun deletePost(
        @PathVariable postId: Long
    ): ApiResponse<Void> {
        adminPostService.deletePost(postId, currentUser)
        return ApiResponse.ok("게시글 삭제 성공", null)
    }

    @PatchMapping("/{postId}/pin")
    @Operation(summary = "게시글 상단 고정", description = "관리자가 특정 게시글을 상단에 고정합니다.")
    fun pinPost(
        @PathVariable postId: Long,
        @Valid @RequestBody request: AdminPostPinRequest
    ): ApiResponse<PostResponse> {
        val response = adminPostService.updatePinStatus(postId, currentUser, request.pinStatus!!)
        return ApiResponse.ok("게시글 고정 상태 변경 성공", response)
    }

    @PatchMapping("/{postId}/status")
    @Operation(summary = "게시글 진행 상태 변경", description = "관리자가 게시글의 진행 상태(ING/CLOSED)를 변경합니다.")
    fun updatePostStatus(
        @PathVariable postId: Long,
        @Valid @RequestBody request: AdminPostStatusRequest
    ): ApiResponse<PostResponse> {
        val response = adminPostService.updatePostStatus(postId, currentUser, request.status!!)
        return ApiResponse.ok("게시글 진행 상태가 변경 성공", response)
    }
}