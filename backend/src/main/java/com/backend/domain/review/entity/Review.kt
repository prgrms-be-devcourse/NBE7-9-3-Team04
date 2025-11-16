package com.backend.domain.review.entity;

import com.backend.domain.resume.entity.Resume;
import com.backend.domain.user.entity.User;
import com.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Review extends BaseEntity {

    @Column(name = "ai_review_content", columnDefinition = "TEXT", nullable = false)
    private String AiReviewContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

}