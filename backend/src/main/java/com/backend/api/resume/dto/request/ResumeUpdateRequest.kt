package com.backend.api.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResumeUpdateRequest(
        @Schema(description = "이력서 내용", example = "수정된 이력서 내용입니다.")
        String content,
        @Schema(description = "기술 스택", example = "Java, Spring Boot, mysql")
        String skill,
        @Schema(description = "대외 활동", example = "수정된 대외 활동 내용입니다.")
        String activity,
        @Schema(description = "자격증", example = "없음")
        String certification,
        @Schema(description = "경력 사항", example = "수정된 경력 사항 내용입니다.")
        String career,
        @Schema(description = "포트폴리오 URL", example = "http://portfolio.example2.com")
        String portfolioUrl
){
}
