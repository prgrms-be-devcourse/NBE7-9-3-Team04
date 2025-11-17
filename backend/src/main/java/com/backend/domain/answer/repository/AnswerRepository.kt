package com.backend.domain.answer.repository

import com.backend.domain.answer.entity.Answer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface AnswerRepository : JpaRepository<Answer, Long> {
    @EntityGraph(attributePaths = ["author", "question"])
    @Query("SELECT a FROM Answer a LEFT JOIN a.feedback f WHERE a.question.id = :questionId AND a.isPublic = true ORDER BY COALESCE(f.aiScore, 0) DESC, a.modifyDate DESC")
    fun findByQuestionIdAndIsPublicTrueOrderByFeedbackScoreDesc(questionId: Long, pageable: Pageable): Page<Answer>

    @EntityGraph(attributePaths = ["author", "question"])
    fun findByAuthorId(authorId: Long, pageable: Pageable): Page<Answer>

    fun findFirstByQuestionIdAndAuthorId(questionId: Long, authorId: Long): Optional<Answer>
}
