package com.backend.api.resume.dto.response;

import com.backend.domain.resume.entity.Resume;
import com.backend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResumeCreateResponse(
        @Schema(description = "이력서 ID", example = "1")
        Long resumeId,
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
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
    public static ResumeCreateResponse from(Resume resume, User user) {
        return new ResumeCreateResponse(
                resume.getId(),
                user.getId(),
                resume.getContent(),
                resume.getSkill(),
                resume.getActivity(),
                resume.getCertification(),
                resume.getCareer(),
                resume.getPortfolioUrl()
        );
    }
}
