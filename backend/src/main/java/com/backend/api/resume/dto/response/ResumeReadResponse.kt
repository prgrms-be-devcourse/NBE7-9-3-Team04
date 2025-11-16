package com.backend.api.resume.dto.response;

import com.backend.domain.resume.entity.Resume;

public record ResumeReadResponse(
        Long id,
        String skill,
        String activity,
        String career,
        String certification,
        String content,
        String portfolioUrl,
        Long userId
) {
    public static ResumeReadResponse from(Resume resume) {
        return new ResumeReadResponse(
                resume.getId(),
                resume.getSkill(),
                resume.getActivity(),
                resume.getCareer(),
                resume.getCertification(),
                resume.getContent(),
                resume.getPortfolioUrl(),
                resume.getUser().getId()
        );
    }
}
