package com.backend.api.comment.controller;

import com.backend.api.comment.dto.request.CommentRequest;
import com.backend.api.comment.dto.response.CommentPageResponse;
import com.backend.api.comment.dto.response.CommentResponse;
import com.backend.api.comment.service.CommentService;
import com.backend.domain.user.entity.User;
import com.backend.global.Rq.Rq;
import com.backend.global.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "CommentController", description = "댓글 API")
public class CommentController {

    private final CommentService commentService;
    private final Rq rq;

    @PostMapping("/{postId}/comments")
    @Operation(summary = "댓글 작성")
    public ApiResponse<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequest reqBody
    ) {
        User currentUser = rq.getUser();

        CommentResponse response = commentService.writeComment(currentUser, postId, reqBody.content());

        return ApiResponse.created(
                "%d번 댓글이 생성되었습니다.".formatted(response.id()),
                response
        );
    }

    @PatchMapping("/{postId}/comments/{commentId}")
    @Operation(summary = "댓글 수정")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentRequest reqBody
    ) {
        User currentUser = rq.getUser();

        CommentResponse response = commentService.updateComment(
                currentUser,
                commentId,
                reqBody.content()
        );

        return ApiResponse.ok(
                "%d번 댓글이 수정되었습니다.".formatted(response.id()),
                response
        );
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long commentId
    ) {
        User currentUser = rq.getUser();

        commentService.deleteComment(currentUser, commentId);

        return ApiResponse.ok(
                "%d번 댓글이 삭제되었습니다.".formatted(commentId),
                null
        );
    }

    @GetMapping("/{postId}/comments")
    @Operation(summary = "댓글 목록 조회")
    public ApiResponse<CommentPageResponse<CommentResponse>> readComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page
    ) {
        User currentUser = null;
        try {
            currentUser = rq.getUser();
        } catch (Exception e) {

        }

        CommentPageResponse<CommentResponse> commentsPage = commentService.getCommentsByPostId(
                currentUser,
                page,
                postId
        );

        return ApiResponse.ok(
                "%d번 게시글의 댓글 목록 조회 성공".formatted(postId),
                commentsPage
        );
    }

}
