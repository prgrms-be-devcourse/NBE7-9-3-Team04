package com.backend.domain.question.repository

import com.backend.api.question.dto.response.AiQuestionReadAllResponse
import com.backend.api.question.dto.response.AiQuestionReadResponse
import com.backend.api.question.dto.response.PortfolioListReadResponse
import com.backend.api.question.dto.response.PortfolioReadResponse
import com.backend.domain.question.entity.QQuestion
import com.backend.domain.question.entity.QuestionCategoryType
import com.backend.domain.user.entity.User
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.UUID

class QuestionRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : QuestionRepositoryCustom {

    private val question = QQuestion.question

    override fun getQuestionByCategoryTypeAndUserId(
        categoryType: QuestionCategoryType,
        user: User
    ): AiQuestionReadAllResponse? {

        val questions: List<AiQuestionReadResponse> = queryFactory
            .select(
                Projections.constructor(
                    AiQuestionReadResponse::class.java,
                    question.groupId,
                    question.title,
                    question.createDate.max().`as`("date"),
                    question.groupId.count().`as`("count")
                )
            )
            .from(question)
            .where(
                question.categoryType.eq(categoryType),
                question.author.eq(user)
            )
            .groupBy(question.groupId, question.title)
            .orderBy(question.createDate.max().desc())
            .fetch()

        if (questions.isEmpty()) return null
        return AiQuestionReadAllResponse.from(questions)
    }

    override fun getByUserAndGroupId(
        user: User,
        groupId: UUID
    ): PortfolioListReadResponse? {

        val questions: List<PortfolioReadResponse> = queryFactory
            .select(
                Projections.constructor(
                    PortfolioReadResponse::class.java,
                    question.id,
                    question.content
                )
            )
            .from(question)
            .where(
                question.groupId.eq(groupId),
                question.author.eq(user)
            )
            .fetch()

        if (questions.isEmpty()) return null

        val count = count(groupId) ?: 0L
        val title = getTitle(groupId) ?: ""

        return PortfolioListReadResponse.from(title, count, questions)
    }

    private fun count(groupId: UUID): Long? =
        queryFactory
            .select(question.groupId.count())
            .from(question)
            .where(question.groupId.eq(groupId))
            .fetchOne()

    private fun getTitle(groupId: UUID): String? =
        queryFactory
            .select(question.title)
            .from(question)
            .where(question.groupId.eq(groupId))
            .groupBy(question.title)
            .fetchOne()
}
