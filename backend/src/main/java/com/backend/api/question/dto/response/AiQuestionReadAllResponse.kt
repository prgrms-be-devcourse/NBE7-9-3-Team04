package com.backend.api.question.dto.response;

import java.util.List;
import java.util.UUID;

public record AiQuestionReadAllResponse(
        List<AiQuestionReadResponse> questions
) {
    public static AiQuestionReadAllResponse from(List<AiQuestionReadResponse> responses){
        return new AiQuestionReadAllResponse(responses);
    }
}