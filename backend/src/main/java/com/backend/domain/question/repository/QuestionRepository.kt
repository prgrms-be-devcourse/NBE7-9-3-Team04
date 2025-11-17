package com.backend.domain.question.repository

import com.backend.domain.question.entity.Question
import com.backend.domain.question.entity.QuestionCategoryType
import com.backend.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuestionRepository : JpaRepository<Question, Long>, QuestionRepositoryCustom {

    @EntityGraph(attributePaths = ["author"])
    fun findByIsApprovedTrue(pageable: Pageable): Page<Question>

    @EntityGraph(attributePaths = ["author"])
    fun findByCategoryTypeAndIsApprovedTrue(
        categoryType: QuestionCategoryType,
        pageable: Pageable
    ): Page<Question>

    @EntityGraph(attributePaths = ["author"])
    @Query(
        """
        SELECT q 
        FROM Question q 
        WHERE q.isApproved = true 
          AND q.categoryType <> :excludedType
        """
    )
    fun findApprovedQuestionsExcludingCategory(
        @Param("excludedType") excludedType: QuestionCategoryType,
        pageable: Pageable
    ): Page<Question>

    @EntityGraph(attributePaths = ["author"])
    @Query(
        """
        SELECT q 
        FROM Question q 
        WHERE q.isApproved = true 
          AND q.categoryType = :categoryType
        """
    )
    fun findApprovedQuestionsByCategory(
        @Param("categoryType") categoryType: QuestionCategoryType,
        pageable: Pageable
    ): Page<Question>

    fun countByAuthor(author: User): Int

    @EntityGraph(attributePaths = ["author"])
    fun findByAuthorId(authorId: Long, pageable: Pageable): Page<Question>
}
