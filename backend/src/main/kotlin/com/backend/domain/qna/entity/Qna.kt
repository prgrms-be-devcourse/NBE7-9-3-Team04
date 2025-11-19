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