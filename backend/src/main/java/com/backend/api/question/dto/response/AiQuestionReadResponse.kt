package com.backend.api.question.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.*


data class AiQuestionReadResponse(
    val groupId: UUID,
    val title: String,
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    val date: LocalDateTime,
    val count: Long
) 