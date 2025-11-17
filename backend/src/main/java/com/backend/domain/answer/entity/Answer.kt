package com.backend.domain.answer.entity

import com.backend.domain.feedback.entity.Feedback
import com.backend.domain.question.entity.Question
import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
class Answer(
    @Column(nullable = false, length = 1000)
    var content: String,

    @Column(nullable = false)
    var isPublic: Boolean,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val author: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(nullable = false)
    val question: Question
) : BaseEntity() {

    @OneToOne(mappedBy = "answer", cascade = [CascadeType.REMOVE])
    var feedback: Feedback? = null

    fun update(content: String?, isPublic: Boolean?) {
        if (content != null && !content.isBlank()) {
            this.content = content
        }
        if (isPublic != null) {
            this.isPublic = isPublic
        }
    }

}
