package com.backend.api.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;


public record ResumeExistResponse(

        @Schema(description = "이력서 존재 여부", example = "true")
        boolean hasResume
) {
    public static ResumeExistResponse from(boolean hasResume) {
        return new ResumeExistResponse(hasResume);
    }
}