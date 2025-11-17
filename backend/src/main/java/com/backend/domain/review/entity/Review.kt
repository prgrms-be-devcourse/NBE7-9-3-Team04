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

    class Builder {
        private lateinit var AiReviewContent: String // non-null
        private var resume: Resume? = null
        private var user: User? = null

        fun AiReviewContent(content: String) = apply { this.AiReviewContent = content }
        fun resume(resume: Resume?) = apply { this.resume = resume }
        fun user(user: User?) = apply { this.user = user }

        fun build(): Review = Review(
            AiReviewContent = AiReviewContent,
            resume = resume,
            user = user
        )
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}