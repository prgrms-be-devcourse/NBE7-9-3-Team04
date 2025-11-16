package com.backend.api.resume.service;

import com.backend.api.resume.dto.request.ResumeCreateRequest;
import com.backend.api.resume.dto.request.ResumeUpdateRequest;
import com.backend.api.resume.dto.response.ResumeCreateResponse;
import com.backend.api.resume.dto.response.ResumeExistResponse;
import com.backend.api.resume.dto.response.ResumeReadResponse;
import com.backend.api.resume.dto.response.ResumeUpdateResponse;
import com.backend.api.user.service.UserService;
import com.backend.domain.resume.entity.Resume;
import com.backend.domain.resume.repository.ResumeRepository;
import com.backend.domain.user.entity.User;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;

    private final UserService userService;

    @Transactional
    public ResumeCreateResponse createResume(Long userId, ResumeCreateRequest request) {
        validateResumeNotExists(userId);

        User user = userService.getUser(userId);

        Resume resume = Resume.builder()
                .user(user)
                .skill(request.skill())
                .activity(request.activity())
                .career(request.career())
                .certification(request.certification())
                .content(request.content())
                .portfolioUrl(request.portfolioUrl())
                .build();

        resumeRepository.save(resume);

        return ResumeCreateResponse.from(resume, user);
    }

    private Boolean hasResume(Long userId) {
        return resumeRepository.existsByUserId(userId);
    }

    private void validateResumeNotExists(Long userId) {
        if (hasResume(userId)) {
            throw new ErrorException(ErrorCode.DUPLICATE_RESUME);
        }
    }

    @Transactional
    public ResumeUpdateResponse updateResume(Long userId,  ResumeUpdateRequest request) {
        User user = userService.getUser(userId);

        Resume resume = getResumeByUser(user);
        validateResumeAuthor(resume, user);
        resume.update(request);

        return ResumeUpdateResponse.from(resume, user);
    }

    public Resume getResume(Long resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_RESUME));
    }

    private void validateResumeAuthor(Resume resume, User user) {
        if (resume.getUser().getId() != user.getId()) {
            throw new ErrorException(ErrorCode.INVALID_USER);
        }
    }

    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        User user = userService.getUser(userId);

        Resume resume = getResume(resumeId);
        validateResumeAuthor(resume, user);

        resumeRepository.delete(resume);
    }

    public ResumeReadResponse readResume(Long userId) {
        User user = userService.getUser(userId);
        Resume resume = getResumeByUser(user);
        return ResumeReadResponse.from(resume);
    }

    public Resume getResumeByUser(User user) {
        return resumeRepository.findByUser(user)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_RESUME));
    }

    public ResumeExistResponse checkResumeExists(Long userId) {
        boolean hasResume = hasResume(userId);
        return ResumeExistResponse.from(hasResume);
    }
}
