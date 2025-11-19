package com.backend.api.comment.controller

import com.backend.api.comment.dto.request.CommentRequest
import com.backend.api.comment.dto.response.CommentPageResponse
import com.backend.api.comment.dto.response.CommentResponse
import com.backend.api.comment.service.CommentService
import com.backend.domain.user.entity.User
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "CommentController", description = "댓글 API")
class CommentController(
    private val commentService: CommentService,
    private val rq: Rq
) {

    @PostMapping("/{postId}/comments")
    @Operation(summary = "댓글 작성")
    fun createComment(
        @PathVariable postId: Long,
        @RequestBody @Valid reqBody: CommentRequest
    ): ApiResponse<CommentResponse> {
        val currentUser = rq.getUser()

        val response = commentService.writeComment(currentUser, postId, reqBody.content!!)

        return ApiResponse.created(
            "${response.id}번 댓글이 생성되었습니다.",
            response
        )
    }

    @PatchMapping("/{postId}/comments/{commentId}")
    @Operation(summary = "댓글 수정")
    fun updateComment(
        @PathVariable commentId: Long,
        @RequestBody @Valid reqBody: CommentRequest
    ): ApiResponse<CommentResponse> {
        val currentUser = rq.getUser()

        val response = commentService.updateComment(
            currentUser,
            commentId,
            reqBody.content!!
        )

        return ApiResponse.ok(
            "${response.id}번 댓글이 수정되었습니다.",
            response
        )
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    fun deleteComment(
        @PathVariable commentId: Long
    ): ApiResponse<Void> {
        val currentUser = rq.getUser()

        commentService.deleteComment(currentUser, commentId)

        return ApiResponse.ok(
            "${commentId}번 댓글이 삭제되었습니다.",
            null
        )
    }

    @GetMapping("/{postId}/comments")
    @Operation(summary = "댓글 목록 조회")
    fun readComments(
        @PathVariable postId: Long,
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<CommentPageResponse<CommentResponse>> {
        val currentUser: User? = runCatching { rq.getUser() }.getOrNull()

        val commentsPage: CommentPageResponse<CommentResponse> = commentService.getCommentsByPostId(
            currentUser,
            page,
            postId
        )

        return ApiResponse.ok(
            "${postId}번 게시글의 댓글 목록 조회 성공",
            commentsPage
        )
    }
}
