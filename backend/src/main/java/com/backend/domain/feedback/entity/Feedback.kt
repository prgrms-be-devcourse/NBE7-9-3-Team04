package com.backend.domain.feedback.entity

import com.backend.domain.answer.entity.Answer
import com.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
class Feedback(
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false)
    var aiScore: Int,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", unique = true) // FK는 Feedback이 갖음
    var answer: Answer
) : BaseEntity() {

    fun update(answer: Answer, score: Int, content: String) {
        this.answer = answer
        this.aiScore = score
        this.content = content
    }

}
