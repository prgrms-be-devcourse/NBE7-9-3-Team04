package com.backend.api.post.service;

import com.backend.api.post.dto.request.PostAddRequest;
import com.backend.api.post.dto.request.PostUpdateRequest;
import com.backend.api.post.dto.response.PostPageResponse;
import com.backend.api.post.dto.response.PostResponse;
import com.backend.api.user.service.UserService;
import com.backend.domain.post.entity.PinStatus;
import com.backend.domain.post.entity.Post;
import com.backend.domain.post.entity.PostCategoryType;
import com.backend.domain.post.entity.PostStatus;
import com.backend.domain.post.repository.PostRepository;
import com.backend.domain.user.entity.User;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.backend.domain.subscription.repository.SubscriptionRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public PostResponse createPost(PostAddRequest request, User user) {
        // 로그인 안한 경우 처리
        if (user == null) {
            throw new ErrorException(ErrorCode.UNAUTHORIZED_USER); // 401
        }

        // 정지된 계정일 경우 처리
        if (!user.validateActiveStatus()) {
            throw new ErrorException(ErrorCode.ACCOUNT_SUSPENDED);
        }

        if(request.pinStatus() == PinStatus.PINNED && !subscriptionRepository.existsByUserAndActiveTrue(user)) {
            throw new ErrorException(ErrorCode.PIN_POST_FORBIDDEN);
        }

        validateDeadline(request.deadline());

        Post post = Post.builder()
                .title(request.title())
                .introduction(request.introduction())
                .content(request.content())
                .deadline(request.deadline())
                .status(request.status())
                .pinStatus(request.pinStatus())
                .recruitCount(request.recruitCount())
                .users(user)
                .postCategoryType(request.categoryType())
                .build();

        Post savedPost = postRepository.save(post);

        return PostResponse.from(savedPost, null);
    }

    public PostResponse getPost(Long postId, User user) {
        Post post = findPostByIdOrThrow(postId);

        boolean isMine = user != null && post.getUsers().getId().equals(user.getId());

        return PostResponse.from(post, isMine);
    }

    @Transactional(readOnly = true)
    public PostPageResponse<PostResponse> getAllPosts(int page) {
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, 9, Sort.by("createDate").descending());

        Page<Post> postsPage  = postRepository.findAll(pageable);

        List<PostResponse> posts = postsPage.getContent()
                .stream()
                .map(Post -> PostResponse.from(Post, null))
                .toList();

        return PostPageResponse.from(postsPage, posts);
    }

    @Transactional(readOnly = true)
    public PostPageResponse<PostResponse> getPostsByUserId(int page, Long userId) {
        User user = userService.getUser(userId);

        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, 15, Sort.by("createDate").descending());
        Page<Post> myPostsPage = postRepository.findByUsers(user, pageable);

        List<PostResponse> myPosts =  myPostsPage
                .getContent()
                .stream()
                .map(post -> PostResponse.from(post, null))
                .toList();

        return PostPageResponse.from(myPostsPage, myPosts);
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, User user) {
        Post post = findPostByIdOrThrow(postId);
        validatePostOwner(post, user);

        validateDeadline(request.deadline());

        post.updatePost(request.title(), request.introduction(),request.content(), request.deadline(), request.status(), request.pinStatus(), request.recruitCount(), request.categoryType());

        return PostResponse.from(post, null);
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = findPostByIdOrThrow(postId);
        validatePostOwner(post, user);

        postRepository.delete(post);
    }

    @Transactional
    public PostResponse closePost(Long postId, User user) {
        Post post = findPostByIdOrThrow(postId);
        validatePostOwner(post, user);

        post.updateStatus(PostStatus.CLOSED);

        return PostResponse.from(post, true);
    }

    public Post findPostByIdOrThrow(Long postId) { // 중복 로직 헬퍼 메서드1
        return postRepository.findById(postId)
                .orElseThrow(() -> new ErrorException(ErrorCode.POST_NOT_FOUND));
    }

    private void validatePostOwner(Post post, User user) { // 중복 로직 헬퍼 메서드2
        if (!post.getUsers().getId().equals(user.getId())) {
            throw new ErrorException(ErrorCode.FORBIDDEN);
        }
    }

    @Transactional(readOnly = true)
    public PostPageResponse<PostResponse> getPostsByCategory(int page, PostCategoryType categoryType) {
        if (page < 1) page = 1;

        Pageable pageable = PageRequest.of(page -1, 9, Sort.by("createDate").descending());
        Page<Post> postsPage = postRepository.findByPostCategoryType(categoryType, pageable);

        if (postsPage.isEmpty()) {
            throw new ErrorException(ErrorCode.POST_NOT_FOUND);
        }

        List<PostResponse> posts =  postsPage.getContent()
                .stream()
                .map(Post -> PostResponse.from(Post, null))
                .toList();

        return PostPageResponse.from(postsPage, posts);
      }

    public List<PostResponse> getPinnedPosts() {
        List<Post> pinnedPosts = postRepository.findByPinStatusAndStatusOrderByCreateDateDesc(PinStatus.PINNED, PostStatus.ING);

        Collections.shuffle(pinnedPosts);
        List<Post> limitedList = pinnedPosts.stream()
                .limit(5)
                .toList();

        return limitedList.stream()
                .map(post -> PostResponse.from(post, null))
                .toList();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void closePostStatus(){
        LocalDateTime now = LocalDateTime.now();

        List<Post> expiredPosts = getByStatusAndDeadlineLessThan(now);
        for(Post post : expiredPosts){
            post.updateStatus(PostStatus.CLOSED);
        }
    }

    public List<Post> getByStatusAndDeadlineLessThan(LocalDateTime now){
        return postRepository.findByStatusAndDeadlineLessThan(PostStatus.ING,now)
                .orElseThrow(() -> new ErrorException(ErrorCode.POST_NOT_FOUND));

    }

    private void validateDeadline(LocalDateTime deadline) {
        if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
            throw new ErrorException(ErrorCode.INVALID_DEADLINE);
        }
    }
}
