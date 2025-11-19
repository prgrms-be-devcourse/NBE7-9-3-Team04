package com.backend.domain.question.repository

import com.backend.api.question.dto.response.AiQuestionReadAllResponse
import com.backend.api.question.dto.response.PortfolioListReadResponse
import com.backend.domain.question.entity.QuestionCategoryType
import com.backend.domain.user.entity.User
import java.util.UUID

interface QuestionRepositoryCustom {

    fun getQuestionByCategoryTypeAndUserId(
        categoryType: QuestionCategoryType,
        user: User
    ): AiQuestionReadAllResponse?

    fun getByUserAndGroupId(
        user: User,
        groupId: UUID
    ): PortfolioListReadResponse?
}
