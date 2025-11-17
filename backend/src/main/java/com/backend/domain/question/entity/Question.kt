package com.backend.domain.question.entity

import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "question")
open class Question(

    @Column(nullable = false, length = 100)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = true)
    var isApproved: Boolean = false,

    @Column(nullable = false)
    var score: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var author: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var categoryType: QuestionCategoryType,

    @Column(name = "group_id", columnDefinition = "BINARY(16)")
    var groupId: UUID? = null

) : BaseEntity() {

    fun updateApproved(isApproved: Boolean) {
        this.isApproved = isApproved
    }

    fun updateScore(newScore: Int) {
        this.score = newScore
    }

    fun updateUserQuestion(title: String, content: String, categoryType: QuestionCategoryType) {
        this.title = title
        this.content = content
        this.categoryType = categoryType
    }

    fun updateAdminQuestion(
        title: String,
        content: String,
        isApproved: Boolean,
        score: Int?,
        categoryType: QuestionCategoryType
    ) {
        this.title = title
        this.content = content
        updateApproved(isApproved)
        score?.let { updateScore(it) }
        this.categoryType = categoryType
    }

    fun changeCategory(categoryType: QuestionCategoryType) {
        this.categoryType = categoryType
    }

    // Builder
    class Builder {
        private var title: String = ""
        private var content: String = ""
        private var author: User = User()
        private var categoryType: QuestionCategoryType = QuestionCategoryType.DATABASE
        private var isApproved: Boolean = false
        private var score: Int = 0
        private var groupId: UUID? = null

        fun title(title: String) = apply { this.title = title }
        fun content(content: String) = apply { this.content = content }
        fun author(author: User) = apply { this.author = author }
        fun categoryType(categoryType: QuestionCategoryType) = apply { this.categoryType = categoryType }
        fun isApproved(isApproved: Boolean) = apply { this.isApproved = isApproved }
        fun score(score: Int) = apply { this.score = score }
        fun groupId(groupId: UUID) = apply { this.groupId = groupId }

        fun build(): Question {
            return Question(
                title = title,
                content = content,
                isApproved = isApproved,
                score = score,
                author = author,
                categoryType = categoryType,
                groupId = groupId
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}