package com.backend.api.post.service

import com.backend.api.post.dto.response.PostPageResponse
import com.backend.api.post.dto.response.PostResponse
import com.backend.api.user.service.AdminUserService
import com.backend.domain.post.entity.PinStatus
import com.backend.domain.post.entity.PostStatus
import com.backend.domain.post.repository.PostRepository
import com.backend.domain.user.entity.User
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminPostService(
    private val postRepository: PostRepository,
    private val postService: PostService,
    private val adminUserService: AdminUserService
) {

    fun getAllPosts(page: Int, admin: User?): PostPageResponse<PostResponse> {
        adminUserService.validateAdminAuthority(admin)

        val pageNum = if (page < 1) 0 else page - 1
        val pageable: Pageable = PageRequest.of(pageNum, 15, Sort.by("createDate").descending())
        val postsPage = postRepository.findAll(pageable)

        val posts = postsPage.content
            .map { post -> PostResponse.from(post, false) }

        return PostPageResponse.from(postsPage, posts)
    }

    fun getPostById(postId: Long, user: User): PostResponse {
        adminUserService.validateAdminAuthority(user)
        val post = postService.findPostByIdOrThrow(postId)
        return PostResponse.from(post, false)
    }

    @Transactional
    fun deletePost(postId: Long, admin: User) {
        adminUserService.validateAdminAuthority(admin)
        val post = postService.findPostByIdOrThrow(postId)
        postRepository.delete(post)
    }

    @Transactional
    fun updatePinStatus(postId: Long, admin: User, status: PinStatus): PostResponse {
        adminUserService.validateAdminAuthority(admin)
        val post = postService.findPostByIdOrThrow(postId)
        post.updatePinStatus(status)

        return PostResponse.from(post, false)
    }

    @Transactional
    fun updatePostStatus(postId: Long, admin: User, status: PostStatus): PostResponse {
        adminUserService.validateAdminAuthority(admin)
        val post = postService.findPostByIdOrThrow(postId)
        post.updateStatus(status)

        return PostResponse.from(post, false)
    }
}