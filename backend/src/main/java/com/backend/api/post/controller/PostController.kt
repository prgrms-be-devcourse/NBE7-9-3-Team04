package com.backend.api.post.controller

import com.backend.api.post.dto.request.PostAddRequest
import com.backend.api.post.dto.request.PostUpdateRequest
import com.backend.api.post.dto.response.PostPageResponse
import com.backend.api.post.dto.response.PostResponse
import com.backend.api.post.service.PostService
import com.backend.domain.post.entity.PostCategoryType
import com.backend.domain.user.entity.User
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/posts")
class PostController(
    private val postService: PostService,
    private val rq: Rq
) {

    private val currentUser: User
        get() = rq.getUser()

    @PostMapping
    @Operation(summary = "게시글 생성", description = "유저가 게시물을 등록합니다.")
    fun createPost(
        @Valid @RequestBody request: PostAddRequest
    ): ApiResponse<PostResponse> {
        val user = currentUser
        val response = postService.createPost(request, user)

        return ApiResponse.ok(
            "${response.postId}번 게시글 등록을 완료했습니다.",
            response
        )
    }

    @GetMapping("/pinned")
    @Operation(summary = "상단 고정 게시글 목록 조회")
    fun getPinnedPosts(): ApiResponse<List<PostResponse>> {
        val postResponseList = postService.getPinnedPosts()

        return ApiResponse.ok(
            "상단 고정된 게시글을 성공적으로 조회했습니다.",
            postResponseList
        )
    }

    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정")
    fun updatePost(
        @PathVariable postId: Long,
        @Valid @RequestBody request: PostUpdateRequest
    ): ApiResponse<PostResponse> {
        val user = currentUser
        val response = postService.updatePost(postId, request, user)

        return ApiResponse.ok(
            "${postId}번 게시글 수정을 완료했습니다.",
            response
        )
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제")
    fun deletePost(@PathVariable postId: Long): ApiResponse<Void> {
        val user = currentUser
        postService.deletePost(postId, user)

        return ApiResponse.ok("게시글 삭제가 완료되었습니다.", null)
    }

    @PostMapping("/{postId}/close")
    @Operation(summary = "모집글 즉시 마감", description = "작성자가 자신의 모집글을 즉시 마감 처리합니다.")
    fun closePost(@PathVariable postId: Long): ApiResponse<PostResponse> {
        val user = currentUser
        val response = postService.closePost(postId, user)
        return ApiResponse.ok("모집글이 마감 처리되었습니다.", response)
    }

    @GetMapping("/category/{categoryType}")
    @Operation(
        summary = "카테고리별 게시글 조회",
        description = "특정 카테고리(ENUM)에 속한 게시글을 조회합니다. 예: /api/v1/posts/category/PROJECT"
    )
    fun getPostsByCategory(
        @PathVariable categoryType: PostCategoryType,
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<PostPageResponse<PostResponse>> {
        val postsPage = postService.getPostsByCategory(page, categoryType)

        return ApiResponse.ok(
            "카테고리별 게시글 조회 성공",
            postsPage
        )
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 단건 조회")
    fun getPost(@PathVariable postId: Long): ApiResponse<PostResponse> {

        val user: User? = runCatching { rq.getUser() }.getOrNull()

        val response = postService.getPost(postId, user)

        return ApiResponse.ok(
            "${postId}번 게시글을 성공적으로 조회했습니다.",
            response
        )
    }

    @GetMapping
    @Operation(summary = "게시글 다건 조회")
    fun getAllPosts(
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<PostPageResponse<PostResponse>> {
        val postsPage = postService.getAllPosts(page)

        return ApiResponse.ok(
            "전체 게시글 조회 성공",
            postsPage
        )
    }
}