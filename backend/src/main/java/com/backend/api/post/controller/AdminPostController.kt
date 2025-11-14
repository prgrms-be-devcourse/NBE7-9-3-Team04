package com.backend.api.post.controller;

import com.backend.api.post.dto.request.AdminPostPinRequest;
import com.backend.api.post.dto.request.AdminPostStatusRequest;
import com.backend.api.post.dto.response.PostPageResponse;
import com.backend.api.post.dto.response.PostResponse;
import com.backend.api.post.service.AdminPostService;
import com.backend.global.Rq.Rq;
import com.backend.global.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/posts")
@Tag(name = "Admin Post", description = "게시글 관리 API(관리자)")
public class AdminPostController {

    private final AdminPostService adminPostService;
    private final Rq rq;

    @GetMapping
    @Operation(summary = "전체 게시글 조회", description = "관리자가 모든 게시글을 조회합니다.")
    public ApiResponse<PostPageResponse<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "1") int page
    ) {
        PostPageResponse<PostResponse> postsPage = adminPostService.getAllPosts(page, rq.getUser());
        return ApiResponse.ok("전체 게시글 조회 성공", postsPage);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 단건 조회", description = "관리자가 특정 게시글을 조회합니다.")
    public ApiResponse<PostResponse> getPostById(
            @PathVariable Long postId) {
        PostResponse response = adminPostService.getPostById(postId, rq.getUser());
        return ApiResponse.ok("게시글 단건 조회 성공", response);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "관리자가 특정 게시글을 삭제합니다.")
    public ApiResponse<Void> deletePost(
            @PathVariable Long postId) {
        adminPostService.deletePost(postId, rq.getUser());
        return ApiResponse.ok("게시글 삭제 성공", null);
    }

    @PatchMapping("/{postId}/pin")
    @Operation(summary = "게시글 상단 고정", description = "관리자가 특정 게시글을 상단에 고정합니다.")
    public ApiResponse<PostResponse> pinPost(
            @PathVariable Long postId,
            @Valid @RequestBody AdminPostPinRequest request) {
        PostResponse response = adminPostService.updatePinStatus(postId, rq.getUser(), request.pinStatus());
        return ApiResponse.ok("게시글 고정 상태 변경 성공", response);
    }

    @PatchMapping("/{postId}/status")
    @Operation(summary = "게시글 진행 상태 변경", description = "관리자가 게시글의 진행 상태(ING/CLOSED)를 변경합니다.")
    public ApiResponse<PostResponse> updatePostStatus(
            @PathVariable Long postId,
            @Valid @RequestBody AdminPostStatusRequest request) {
        PostResponse response = adminPostService.updatePostStatus(postId, rq.getUser(), request.status());
        return ApiResponse.ok("게시글 진행 상태가 변경 성공", response);
    }
}
