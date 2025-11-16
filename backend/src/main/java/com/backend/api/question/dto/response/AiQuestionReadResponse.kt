package com.backend.api.question.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AiQuestionReadResponse(
    UUID groupId,
    String title,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime date,
    Long count
) {
}