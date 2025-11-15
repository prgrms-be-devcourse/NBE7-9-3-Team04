package com.backend.domain.userQuestion.entity

import com.backend.domain.question.entity.Question
import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import jakarta.persistence.*

//user_id, question_id를 FK키로 가지는 테이블
//동일한 문제 풀이에 대해서는 새로 생성하지 않고 갱신하도록 한다.
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "question_id"])])
class UserQuestion(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: Question,

    @Column(nullable = false)
    var aiScore: Int
) : BaseEntity() {

    fun updateAiScoreIfHigher(newAiScore: Int?) {

        if (newAiScore != null && newAiScore > this.aiScore) {
            this.aiScore = newAiScore
        }
    }
}