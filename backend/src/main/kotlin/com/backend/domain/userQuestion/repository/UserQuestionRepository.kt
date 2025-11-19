package com.backend.domain.userQuestion.repository

import com.backend.domain.question.entity.Question
import com.backend.domain.user.entity.User
import com.backend.domain.userQuestion.entity.UserQuestion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserQuestionRepository : JpaRepository<UserQuestion, Long> {
    fun findByUserAndQuestion(user: User, question: Question): UserQuestion?
    fun findByUser_Id(userId: Long): List<UserQuestion>
    fun existsByUserAndQuestion(user: User, question: Question): Boolean

    //JPQL로 합계 계산
    @Query("SELECT SUM(uq.aiScore) FROM UserQuestion uq WHERE uq.user = :user")
    fun sumAiScoreByUser(@Param("user") user: User): Int?

    fun countByUser(user: User): Int
}
