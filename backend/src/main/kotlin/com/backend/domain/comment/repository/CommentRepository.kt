package com.backend.domain.comment.repository

import com.backend.domain.comment.entity.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = ["author", "post"])
    fun findByPostId(postId: Long, pageable: Pageable): Page<Comment>

    @EntityGraph(attributePaths = ["author", "post"])
    fun findByAuthorId(authorId: Long, pageable: Pageable): Page<Comment>
}
