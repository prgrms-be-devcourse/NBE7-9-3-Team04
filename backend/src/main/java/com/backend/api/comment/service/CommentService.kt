package com.backend.api.comment.service

import com.backend.api.comment.dto.response.CommentMypageResponse
import com.backend.api.comment.dto.response.CommentPageResponse
import com.backend.api.comment.dto.response.CommentResponse
import com.backend.api.post.service.PostService
import com.backend.api.user.service.UserService
import com.backend.domain.comment.entity.Comment
import com.backend.domain.comment.repository.CommentRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.global.Rq.Rq
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentRepository: CommentRepository,
    private val postService: PostService,
    private val userService: UserService,
    private val rq: Rq
) {
    fun findByIdOrThrow(id: Long): Comment {
        return commentRepository.findById(id)
            .orElseThrow { ErrorException(ErrorCode.COMMENT_NOT_FOUND) }!!
    }

    @Transactional
    fun writeComment(currentUser: User, postId: Long, content: String): CommentResponse {
        if (!currentUser.validateActiveStatus()) {
            throw ErrorException(ErrorCode.ACCOUNT_SUSPENDED)
        }

        val post = postService.findPostByIdOrThrow(postId)

        val comment = Comment(
            content,
            post,
            currentUser,
        )

        val savedComment = commentRepository.save(comment)

        return CommentResponse.from(savedComment, null)
    }

    @Transactional
    fun updateComment(currentUser: User, commentId: Long, newContent: String): CommentResponse {
        val comment = this.findByIdOrThrow(commentId)

        if (comment.author.id  != currentUser.id) {
            throw ErrorException(ErrorCode.COMMENT_INVALID_USER)
        }

        comment.updateContent(newContent)

        return CommentResponse.from(comment, null)
    }

    @Transactional
    fun deleteComment(currentUser: User, commentId: Long) {
        val comment = this.findByIdOrThrow(commentId)

        if (comment.author.id  != currentUser.id) {
            throw ErrorException(ErrorCode.COMMENT_INVALID_USER)
        }

        commentRepository.delete(comment)
    }

    fun getCommentsByPostId(currentUser: User?, page: Int, postId: Long): CommentPageResponse<CommentResponse> {
        var page = page
        postService.findPostByIdOrThrow(postId)

        if (page < 1) page = 1
        val pageable: Pageable = PageRequest.of(page - 1, 20, Sort.by("createDate").ascending())

        val commentsPage: Page<Comment> = commentRepository.findByPostId(postId, pageable)

        val currentUserId: Long? = currentUser?.id

        val comments = commentsPage.getContent()
            .map  { comment: Comment ->
                val isMine = comment.author.id == currentUserId
                CommentResponse.from(comment, isMine)
            }

        return CommentPageResponse.from(commentsPage, comments)
    }

    fun getCommentsByUserId(page: Int, userId: Long): CommentPageResponse<CommentMypageResponse> {
        var page = page
        userService.getUser(userId)
        val currentUser = rq.getUser()
        if (currentUser.id != userId && currentUser.role != Role.ADMIN) {
            throw ErrorException(ErrorCode.COMMENT_INVALID_USER)
        }

        if (page < 1) page = 1
        val pageable: Pageable = PageRequest.of(page - 1, 15, Sort.by("createDate").descending())
        val userCommentsPage: Page<Comment> = commentRepository.findByAuthorId(userId, pageable)

        val userComments = userCommentsPage.getContent()
            .map { comment: Comment -> CommentMypageResponse.from(comment) }

        return CommentPageResponse.from(userCommentsPage, userComments)
    }
}