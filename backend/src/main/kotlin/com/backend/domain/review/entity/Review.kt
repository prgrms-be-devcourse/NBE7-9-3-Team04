package com.backend.domain.review.entity

import com.backend.domain.resume.entity.Resume
import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import jakarta.persistence.*


@Entity
class Review(

    @Column(name = "ai_review_content", columnDefinition = "TEXT", nullable = false)
    var AiReviewContent: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var resume: Resume?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var user: User?

) : BaseEntity() {
}