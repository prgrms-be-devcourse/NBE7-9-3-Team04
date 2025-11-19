package com.backend.api.qna.dto.request

import com.backend.domain.qna.entity.QnaCategoryType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class QnaAddRequest(

    @field:NotBlank(message = "Qna 제목은 필수입니다.")
    @field:Schema(description = "Qna 제목", example = "로그인이 자꾸 실패합니다.")
    val title: String,

    @field:NotBlank(message = "Qna 내용은 필수입니다.")
    @field:Schema(
        description = "Qna 내용",
        example = "회원가입은 정상적으로 되었는데, 로그인 시 '이메일 또는 비밀번호가 올바르지 않습니다'라는 문구가 계속 나옵니다."
    )
    val content: String,

    @field:Schema(description = "Qna 카테고리 타입", example = "ACCOUNT")
    val categoryType: QnaCategoryType?
)
