package com.backend.domain.feedback.entity

import com.backend.domain.answer.entity.Answer
import com.backend.global.entity.BaseEntity
import jakarta.persistence.*
import lombok.AccessLevel
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.NoArgsConstructor

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
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var content: String = ""
        private var aiScore: Int = 0
        private lateinit var answer: Answer

        fun content(content: String) = apply { this.content = content }
        fun aiScore(aiScore: Int) = apply { this.aiScore = aiScore }
        fun answer(answer: Answer) = apply { this.answer = answer }

        fun build() = Feedback(content, aiScore, answer)
    }

}
