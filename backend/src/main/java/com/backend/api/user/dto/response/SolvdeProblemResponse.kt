package com.backend.api.user.dto.response

import java.time.LocalDateTime

data class SolvedProblemResponse (
    val title: String,// 문제 제목
    val modifyDate: LocalDateTime?// 수정일
)