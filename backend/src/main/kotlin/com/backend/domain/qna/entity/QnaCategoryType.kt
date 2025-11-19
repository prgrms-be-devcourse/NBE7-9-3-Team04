package com.backend.domain.qna.entity

enum class QnaCategoryType(
    val displayName: String
) {
    ACCOUNT("계정"),
    PAYMENT("결제"),
    SYSTEM("시스템"),
    RECRUITMENT("모집"),
    SUGGESTION("제안"),
    OTHER("기타");
}
