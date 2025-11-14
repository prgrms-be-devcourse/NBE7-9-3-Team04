package com.backend.api.post.service;

import com.backend.api.post.dto.response.PostPageResponse;
import com.backend.api.post.dto.response.PostResponse;
import com.backend.api.user.service.AdminUserService;
import com.backend.domain.post.entity.PinStatus;
import com.backend.domain.post.entity.Post;
import com.backend.domain.post.entity.PostStatus;
import com.backend.domain.post.repository.PostRepository;
import com.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPostService {

    private final PostRepository postRepository;
    private final PostService postService;
    private final AdminUserService adminUserService;

    public PostPageResponse<PostResponse> getAllPosts(int page, User admin) {
        adminUserService.validateAdminAuthority(admin);

        if(page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, 15, Sort.by("createDate").descending());
        Page<Post> postsPage = postRepository.findAll(pageable);

        List<PostResponse> posts =  postsPage.getContent()
                .stream()
                .map(post -> PostResponse.from(post, null))
                .toList();

        return PostPageResponse.from(postsPage, posts);
    }

    public PostResponse getPostById(Long postId, User user) {
        adminUserService.validateAdminAuthority(user);
        Post post = postService.findPostByIdOrThrow(postId);
        return PostResponse.from(post, null);
    }

    @Transactional
    public void deletePost(Long postId, User admin) {
        adminUserService.validateAdminAuthority(admin);
        Post post = postService.findPostByIdOrThrow(postId);
        postRepository.delete(post);
    }

    @Transactional
    public PostResponse updatePinStatus(Long postId, User admin, PinStatus status) {
        adminUserService.validateAdminAuthority(admin);
        Post post = postService.findPostByIdOrThrow(postId);
        post.updatePinStatus(status);

        return PostResponse.from(post, null);
    }

    @Transactional
    public PostResponse updatePostStatus(Long postId, User admin, PostStatus status) {
        adminUserService.validateAdminAuthority(admin);
        Post post =postService.findPostByIdOrThrow(postId);
        post.updateStatus(status);

        return PostResponse.from(post, null);
    }
}
