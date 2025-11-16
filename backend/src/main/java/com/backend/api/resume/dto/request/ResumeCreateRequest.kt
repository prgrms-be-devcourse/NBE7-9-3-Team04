package com.backend.api.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResumeCreateRequest(
    @Schema(description = "이력서 내용", example = "이력서 내용입니다.")
    String content,
    @Schema(description = "기술 스택", example = "Java, Spring Boot")
    String skill,
    @Schema(description = "대외 활동", example = "대외 활동 내용입니다.")
    String activity,
    @Schema(description = "자격증", example = "없음")
    String certification,
    @Schema(description = "경력 사항", example = "경력 사항 내용입니다.")
    String career,
    @Schema(description = "포트폴리오 URL", example = "http://portfolio.example.com")
    String portfolioUrl
) {
}
