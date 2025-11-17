package com.backend.domain.post.repository

import com.backend.domain.post.entity.PinStatus
import com.backend.domain.post.entity.Post
import com.backend.domain.post.entity.PostCategoryType
import com.backend.domain.post.entity.PostStatus
import com.backend.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface PostRepository : JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = ["users"])
    fun findByPostCategoryType(categoryType: PostCategoryType, pageable: Pageable): Page<Post>

    @EntityGraph(attributePaths = ["users"])
    fun findByUsers(user: User, pageable: Pageable): Page<Post>

    @EntityGraph(attributePaths = ["users"])
    fun findByPinStatusAndStatusOrderByCreateDateDesc(pinStatus: PinStatus, status: PostStatus): List<Post>

    fun findByStatusAndDeadlineLessThan(status: PostStatus, now: LocalDateTime):List<Post>
}
