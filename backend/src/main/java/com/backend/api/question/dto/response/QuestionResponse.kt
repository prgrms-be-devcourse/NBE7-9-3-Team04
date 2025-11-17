package com.backend.api.question.dto.response

import com.backend.domain.question.entity.Question
import com.backend.domain.question.entity.QuestionCategoryType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class QuestionResponse(

    @field:Schema(description = "질문 ID", example = "1")
    val questionId: Long,

    @field:Schema(description = "질문 제목", example = "Spring Bean의 생명주기는 어떻게 되나요?")
    val title: String,

    @field:Schema(description = "질문 내용", example = "Bean의 생성과 소멸 과정에 대해 설명해주세요.")
    val content: String,

    @field:Schema(description = "승인 여부", example = "false")
    val isApproved: Boolean,

    @field:Schema(description = "점수", example = "5")
    val score: Int,

    @field:Schema(description = "작성자 ID", example = "1")
    val authorId: Long,

    @field:Schema(description = "작성자 닉네임", example = "user123")
    val authorNickname: String,

    @field:Schema(description = "카테고리 타입", example = "SPRING")
    val categoryType: QuestionCategoryType?,

    @field:Schema(description = "작성일", example = "2025-10-13T11:00:00")
    val createdDate: LocalDateTime,

    @field:Schema(description = "수정일", example = "2025-10-13T12:00:00")
    val modifiedDate: LocalDateTime
) {
    companion object {
        fun from(question: Question): QuestionResponse =
            QuestionResponse(
                questionId = question.id,
                title = question.title,
                content = question.content,
                isApproved = question.isApproved ?: false,
                score = question.score ?: 0,
                authorId = question.author.id,
                authorNickname = question.author.nickname,
                categoryType = question.categoryType,
                createdDate = question.createDate,
                modifiedDate = question.modifyDate
            )
    }
}
