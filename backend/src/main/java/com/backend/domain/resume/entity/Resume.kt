package com.backend.domain.resume.entity;

import com.backend.api.resume.dto.request.ResumeUpdateRequest;
import com.backend.domain.user.entity.User;
import com.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class Resume extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String content;  // 이력서 내용

    private String skill;   // 기술 스택

    @Column(columnDefinition = "TEXT")
    private String activity;  // 대외 활동

    @Column(columnDefinition = "TEXT")
    private String certification;  // 자격증

    @Column(columnDefinition = "TEXT")
    private String career;  // 경력 사항

    private String portfolioUrl; // 포트폴리오 URL

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 이력서 소유자

    public void update(ResumeUpdateRequest request) {
        this.skill = request.skill();
        this.activity = request.activity();
        this.career = request.career();
        this.certification = request.certification();
        this.content = request.content();
        this.portfolioUrl = request.portfolioUrl();
    }
}
