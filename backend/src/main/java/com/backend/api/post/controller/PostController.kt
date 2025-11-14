package com.backend.api.post.controller;

import com.backend.api.post.dto.request.PostAddRequest;
import com.backend.api.post.dto.request.PostUpdateRequest;
import com.backend.api.post.dto.response.PostPageResponse;
import com.backend.api.post.dto.response.PostResponse;
import com.backend.api.post.service.PostService;
import com.backend.domain.post.entity.PostCategoryType;
import com.backend.domain.user.entity.User;
import com.backend.global.Rq.Rq;
import com.backend.global.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/posts")
public class PostController {
    private final PostService postService;
    private final Rq rq;

    @PostMapping
    @Operation(summary = "게시글 생성", description = "유저가 게시물을 등록합니다.")
    public ApiResponse<PostResponse> createPost(
            @Valid @RequestBody PostAddRequest request) {

        User user = getCurrentUser();
        PostResponse response = postService.createPost(request, user);

        return ApiResponse.ok(
                "%d번 게시글 등록을 완료했습니다.".formatted(response.postId()),
                response
        );
    }

    @GetMapping("/pinned")
    @Operation(summary = "상단 고정 게시글 목록 조회")
    public ApiResponse<List<PostResponse>> getPinnedPosts() {
        List<PostResponse> postResponseList = postService.getPinnedPosts();

        return ApiResponse.ok(
                "상단 고정된 게시글을 성공적으로 조회했습니다.",
                postResponseList
        );
    }

    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정")
    public ApiResponse<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {

        User user = getCurrentUser();
        PostResponse response = postService.updatePost(postId, request, user);

        return ApiResponse.ok(
                "%d번 게시글 수정을 완료했습니다.".formatted(postId),
                response
        );
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제")
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {

        User user = getCurrentUser();
        postService.deletePost(postId, user);

        return ApiResponse.ok("게시글 삭제가 완료되었습니다.", null);
    }

    @PostMapping("/{postId}/close")
    @Operation(summary = "모집글 즉시 마감", description = "작성자가 자신의 모집글을 즉시 마감 처리합니다.")
    public ApiResponse<PostResponse> closePost(@PathVariable Long postId) {
        User user = getCurrentUser();
        PostResponse response = postService.closePost(postId, user);
        return ApiResponse.ok("모집글이 마감 처리되었습니다.", response);
    }

    @GetMapping("/category/{categoryType}")
    @Operation(
            summary = "카테고리별 게시글 조회",
            description = "특정 카테고리(ENUM)에 속한 게시글을 조회합니다. 예: /api/v1/posts/category/PROJECT"
    )
    public ApiResponse<PostPageResponse<PostResponse>> getPostsByCategory(
            @PathVariable PostCategoryType categoryType,
            @RequestParam(defaultValue = "1") int page
    ) {
        PostPageResponse<PostResponse> postsPage = postService.getPostsByCategory(page, categoryType);

        return ApiResponse.ok(
                "카테고리별 게시글 조회 성공",
                postsPage
        );
    }

    private User getCurrentUser() {
        return rq.getUser();
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 단건 조회")
    public ApiResponse<PostResponse> getPost(@PathVariable Long postId) {
        User user = null;
        try {
            user = rq.getUser();
        } catch (Exception e) {
        }

        PostResponse response = postService.getPost(postId, user);

        return ApiResponse.ok(
                "%d번 게시글을 성공적으로 조회했습니다.".formatted(postId),
                response
        );
    }

    @GetMapping
    @Operation(summary = "게시글 다건 조회")
    public ApiResponse<PostPageResponse<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "1.0") int page
    ) {
        PostPageResponse<PostResponse> postsPage = postService.getAllPosts(page);

        return ApiResponse.ok(
                "전체 게시글 조회 성공",
                postsPage
        );
    }
}
