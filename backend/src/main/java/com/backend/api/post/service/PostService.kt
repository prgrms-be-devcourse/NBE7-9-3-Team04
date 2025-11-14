package com.backend.api.post.service

import com.backend.api.post.dto.request.PostAddRequest
import com.backend.api.post.dto.request.PostUpdateRequest
import com.backend.api.post.dto.response.PostPageResponse
import com.backend.api.post.dto.response.PostResponse
import com.backend.api.user.service.UserService
import com.backend.domain.post.entity.PinStatus
import com.backend.domain.post.entity.Post
import com.backend.domain.post.entity.PostCategoryType
import com.backend.domain.post.entity.PostStatus
import com.backend.domain.post.repository.PostRepository
import com.backend.domain.subscription.repository.SubscriptionRepository
import com.backend.domain.user.entity.User
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PostService(
    private val postRepository: PostRepository,
    private val userService: UserService,
    private val subscriptionRepository: SubscriptionRepository
) {
    @Transactional
    fun createPost(request: PostAddRequest, user: User?): PostResponse {

        if (user == null) {
            throw ErrorException(ErrorCode.UNAUTHORIZED_USER) // 401
        }

        // 정지된 계정일 경우 처리
        if (!user.validateActiveStatus()) {
            throw ErrorException(ErrorCode.ACCOUNT_SUSPENDED)
        }

        if (request.pinStatus == PinStatus.PINNED && !subscriptionRepository.existsByUserAndActiveTrue(user)) {
            throw ErrorException(ErrorCode.PIN_POST_FORBIDDEN)
        }

        validateDeadline(request.deadline)

        val post = Post.builder()
            .title(request.title)
            .introduction(request.introduction)
            .content(request.content)
            .deadline(request.deadline)
            .status(request.status)
            .pinStatus(request.pinStatus)
            .recruitCount(request.recruitCount)
            .users(user)
            .postCategoryType(request.categoryType)
            .build()

        val savedPost = postRepository.save(post)

        return PostResponse.from(savedPost, false)
    }

    fun getPost(postId: Long, user: User?): PostResponse {
        val post = findPostByIdOrThrow(postId)

        val isMine = user != null && post.users.id == user.id

        return PostResponse.from(post, isMine)
    }

    @Transactional(readOnly = true)
    fun getAllPosts(page: Int): PostPageResponse<PostResponse> {
        var pageNum = if (page < 1) 0 else page - 1
        val pageable: Pageable = PageRequest.of(pageNum, 9, Sort.by("createDate").descending())

        val postsPage = postRepository.findAll(pageable)

        val posts = postsPage.content
            .map { PostResponse.from(it, false) }

        return PostPageResponse.from(postsPage, posts)
    }

    @Transactional(readOnly = true)
    fun getPostsByUserId(page: Int, userId: Long): PostPageResponse<PostResponse> {
        val user = userService.getUser(userId)

        var pageNum = if (page < 1) 0 else page - 1
        val pageable: Pageable = PageRequest.of(pageNum, 15, Sort.by("createDate").descending())
        val myPostsPage = postRepository.findByUsers(user, pageable)

        val myPosts = myPostsPage.content
            .map { post -> PostResponse.from(post, true) }

        return PostPageResponse.from(myPostsPage, myPosts)
    }

    @Transactional
    fun updatePost(postId: Long, request: PostUpdateRequest, user: User): PostResponse {
        val post = findPostByIdOrThrow(postId)
        validatePostOwner(post, user)

        validateDeadline(request.deadline)

        post.updatePost(
            request.title,
            request.introduction,
            request.content,
            request.deadline,
            request.status,
            request.pinStatus,
            request.recruitCount,
            request.categoryType
        )

        return PostResponse.from(post, true)
    }

    @Transactional
    fun deletePost(postId: Long, user: User) {
        val post = findPostByIdOrThrow(postId)
        validatePostOwner(post, user)

        postRepository.delete(post)
    }

    @Transactional
    fun closePost(postId: Long, user: User): PostResponse {
        val post = findPostByIdOrThrow(postId)
        validatePostOwner(post, user)

        post.updateStatus(PostStatus.CLOSED)

        return PostResponse.from(post, true)
    }

    fun findPostByIdOrThrow(postId: Long): Post {
        return postRepository.findById(postId)
            .orElseThrow { ErrorException(ErrorCode.POST_NOT_FOUND) }
    }

    private fun validatePostOwner(post: Post, user: User) {
        if (post.users.id != user.id) {
            throw ErrorException(ErrorCode.FORBIDDEN)
        }
    }

    @Transactional(readOnly = true)
    fun getPostsByCategory(page: Int, categoryType: PostCategoryType): PostPageResponse<PostResponse> {
        var pageNum = if (page < 1) 0 else page - 1

        val pageable: Pageable = PageRequest.of(pageNum, 9, Sort.by("createDate").descending())
        val postsPage = postRepository.findByPostCategoryType(categoryType, pageable)

        if (postsPage.isEmpty) {
            throw ErrorException(ErrorCode.POST_NOT_FOUND)
        }

        val posts = postsPage.content
            .map { PostResponse.from(it, false) }

        return PostPageResponse.from(postsPage, posts)
    }

    fun getPinnedPosts(): List<PostResponse> {
        val pinnedPosts = postRepository.findByPinStatusAndStatusOrderByCreateDateDesc(PinStatus.PINNED, PostStatus.ING)

        val limitedList = pinnedPosts.shuffled().take(5)

        return limitedList.map { post -> PostResponse.from(post, false) }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun closePostStatus() {
        val now = LocalDateTime.now()

        val expiredPosts = getByStatusAndDeadlineLessThan(now)
        for (post in expiredPosts) {
            post.updateStatus(PostStatus.CLOSED)
        }
    }

    fun getByStatusAndDeadlineLessThan(now: LocalDateTime): List<Post> {
        return postRepository.findByStatusAndDeadlineLessThan(PostStatus.ING, now)
            .orElseThrow { ErrorException(ErrorCode.POST_NOT_FOUND) }
    }

    private fun validateDeadline(deadline: LocalDateTime) {
        if (deadline.isBefore(LocalDateTime.now())) {
            throw ErrorException(ErrorCode.INVALID_DEADLINE)
        }
    }
}