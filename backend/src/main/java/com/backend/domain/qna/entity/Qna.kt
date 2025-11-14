package com.backend.domain.qna.entity

import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "qna")
class Qna(

    @Column(nullable = false, length = 100)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var author: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var categoryType: QnaCategoryType? = null,

    @Column(nullable = true, columnDefinition = "TEXT")
    var adminAnswer: String? = null,

    @Column(nullable = false)
    var isAnswered: Boolean = false

) : BaseEntity() {

    // JPA 기본 생성자
    protected constructor() : this(
        title = "",
        content = "",
        author = User(),   // JPA용 dummy 객체 (실제 사용 X)
        categoryType = null,
        adminAnswer = null,
        isAnswered = false
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var title: String = ""
        private var content: String = ""
        private var author: User? = null
        private var categoryType: QnaCategoryType? = null

        fun title(title: String) = apply { this.title = title }
        fun content(content: String) = apply { this.content = content }
        fun author(author: User) = apply { this.author = author }
        fun categoryType(categoryType: QnaCategoryType?) = apply { this.categoryType = categoryType }

        fun build() = Qna(
            title = title,
            content = content,
            author = author!!,
            categoryType = categoryType
        )
    }

    fun updateQna(
        title: String,
        content: String,
        categoryType: QnaCategoryType?
    ) {
        this.title = title
        this.content = content
        this.categoryType = categoryType
    }

    fun registerAnswer(answer: String) {
        this.adminAnswer = answer
        this.isAnswered = true
    }
}
