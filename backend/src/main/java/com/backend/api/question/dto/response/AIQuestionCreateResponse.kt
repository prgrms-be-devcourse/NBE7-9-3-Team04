package com.backend.api.question.dto.response;

import java.util.List;
import java.util.UUID;

public record AIQuestionCreateResponse(
        UUID groudId,
        List<AiQuestionResponse> questions
) {
    public static AIQuestionCreateResponse from(UUID groudId, List<AiQuestionResponse> responses){
        return new AIQuestionCreateResponse(groudId,responses);
    }
}
