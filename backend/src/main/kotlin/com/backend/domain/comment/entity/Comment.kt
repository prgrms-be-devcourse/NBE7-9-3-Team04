package com.backend.domain.comment.entity

import com.backend.domain.post.entity.Post
import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(
    indexes = [Index(
        name = "idx_comment_post_create",
        columnList = "post_id, create_date"
    ), Index(name = "idx_comment_author_create", columnList = "author_id, create_date")]
)
class Comment(

    @Column(nullable = false, length = 500)
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(nullable = false)
    var post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    var author: User

) : BaseEntity() {
    fun updateContent(content: String) {
        this.content = content
    }
}
