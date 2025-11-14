package com.backend.api.answer.dto.response

import com.backend.domain.answer.entity.Answer
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JvmRecord
data class AnswerReadResponse(
    @field:Schema(description = "면접 답변 ID", example = "1")
    val id: Long?,
    @field:Schema(description = "작성일", example = "2025-10-13T11:00:00")
    val createDate: LocalDateTime?,
    @field:Schema(description = "수정일", example = "2025-10-13T12:00:00")
    val modifyDate: LocalDateTime?,
    @field:Schema(description = "면접 답변 내용", example = "이것은 면접 답변입니다.")
    val content: String,
    @field:Schema(description = "답변 공개 여부", example = "true")
    val isPublic: Boolean,
    @field:Schema(description = "작성자 ID", example = "1")
    val authorId: Long?,
    @field:Schema(description = "작성자 닉네임", example = "user123")
    val authorNickName: String,
    @field:Schema(description = "질문 ID", example = "1")
    val questionId: Long?
) {
    companion object {
        @JvmStatic
        fun from(answer: Answer) = AnswerReadResponse(
            id = answer.id,
            createDate = answer.createDate,
            modifyDate = answer.modifyDate,
            content = answer.content,
            isPublic = answer.isPublic,
            authorId = answer.author.id,
            authorNickName = answer.author.nickname,
            questionId = answer.question.id
        )
    }
}
