package com.backend.api.qna.dto.response

import com.backend.domain.qna.entity.Qna
import com.backend.domain.qna.entity.QnaCategoryType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class QnaResponse(

    @field:Schema(description = "Qna ID", example = "1")
    val qnaId: Long,

    @field:Schema(description = "Qna 제목", example = "로그인이 자꾸 실패합니다.")
    val title: String,

    @field:Schema(
        description = "Qna 내용",
        example = "회원가입은 정상적으로 되었는데, 로그인 시 '이메일 또는 비밀번호가 올바르지 않습니다'라는 문구가 계속 나옵니다."
    )
    val content: String,

    @field:Schema(description = "작성자 ID", example = "1")
    val authorId: Long,

    @field:Schema(description = "작성자 닉네임", example = "user123")
    val authorNickname: String,

    @field:Schema(description = "카테고리 타입", example = "ACCOUNT")
    val categoryType: QnaCategoryType?,

    @field:Schema(description = "카테고리 이름", example = "계정")
    val categoryName: String?,

    @field:Schema(description = "작성일", example = "2025-10-13T11:00:00")
    val createdDate: LocalDateTime,

    @field:Schema(description = "수정일", example = "2025-10-13T12:00:00")
    val modifiedDate: LocalDateTime,

    @field:Schema(
        description = "관리자 답변 내용",
        example = "비밀번호 재설정 후에도 문제가 지속된다면 고객센터로 문의 부탁드립니다."
    )
    val adminAnswer: String?,

    @field:Schema(description = "관리자 답변 여부", example = "false")
    val isAnswered: Boolean
) {
    companion object {
        fun from(qna: Qna): QnaResponse {
            return QnaResponse(
                qnaId = qna.id!!,
                title = qna.title,
                content = qna.content,
                authorId = qna.author.id!!,
                authorNickname = qna.author.nickname,
                categoryType = qna.categoryType,
                categoryName = qna.categoryType?.displayName,
                createdDate = qna.createDate,
                modifiedDate = qna.modifyDate,
                adminAnswer = qna.adminAnswer,
                isAnswered = qna.isAnswered
            )
        }
    }
}
