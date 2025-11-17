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

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    // 임시 Builder
    class Builder {
        private var title: String = ""
        private var content: String = ""
        private var author: User = User()
        private var categoryType: QnaCategoryType? = null

        fun title(title: String) = apply { this.title = title }
        fun content(content: String) = apply { this.content = content }
        fun author(author: User) = apply { this.author = author }
        fun categoryType(categoryType: QnaCategoryType?) = apply { this.categoryType = categoryType }

        fun build(): Qna {
            val nonNullAuthor = requireNotNull(author) { "Author must not be null" }

            return Qna(
                title = title,
                content = content,
                author = nonNullAuthor,
                categoryType = categoryType
            )
        }
    }

    // 수정 기능
    fun updateQna(
        title: String,
        content: String,
        categoryType: QnaCategoryType?
    ) {
        this.title = title
        this.content = content
        this.categoryType = categoryType
    }

    // 관리자 답변 등록
    fun registerAnswer(answer: String) {
        this.adminAnswer = answer
        this.isAnswered = true
    }
}