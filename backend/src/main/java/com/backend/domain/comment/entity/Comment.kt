package com.backend.domain.comment.entity;

import com.backend.domain.post.entity.Post;
import com.backend.domain.user.entity.User;
import com.backend.global.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        indexes = {
                @Index(name = "idx_comment_post_create", columnList = "post_id, create_date"),
                @Index(name = "idx_comment_author_create", columnList = "author_id, create_date")
        }
)
public class Comment extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User author;

    public void updateContent(String content) {
        this.content = content;
    }

}
