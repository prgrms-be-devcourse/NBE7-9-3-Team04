package com.backend.api.resume.service

import com.backend.api.resume.dto.request.ResumeCreateRequest
import com.backend.api.resume.dto.request.ResumeUpdateRequest
import com.backend.api.resume.dto.response.ResumeCreateResponse
import com.backend.api.resume.dto.response.ResumeExistResponse
import com.backend.api.resume.dto.response.ResumeReadResponse
import com.backend.api.resume.dto.response.ResumeUpdateResponse
import com.backend.api.user.service.UserService
import com.backend.domain.resume.entity.Resume

import com.backend.domain.resume.repository.ResumeRepository
import com.backend.domain.user.entity.User
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ResumeService(
    private val resumeRepository: ResumeRepository,
    private val userService: UserService
) {

    @Transactional
    fun createResume(userId: Long, request: ResumeCreateRequest): ResumeCreateResponse {
        validateResumeNotExists(userId)

        val user = userService.getUser(userId)

        val resume = Resume(
            request.content,
            request.skill,
            request.activity,
            request.certification,
            request.career,
            request.portfolioUrl,
            user
        )

        resumeRepository.save(resume)

        return ResumeCreateResponse.from(resume, user)
    }

    fun hasResume(userId: Long) : Boolean {
        return resumeRepository.existsByUserId(userId)
    }

    fun validateResumeNotExists(userId: Long) {
        if (resumeRepository.existsByUserId(userId)) {
            throw ErrorException(ErrorCode.DUPLICATE_RESUME)
        }
    }

    @Transactional
    fun updateResume(userId: Long, request: ResumeUpdateRequest): ResumeUpdateResponse {
        val user = userService.getUser(userId)

        val resume = getResumeByUser(user)
        validateResumeAuthor(resume, user)
        resume.update(request)

        return ResumeUpdateResponse.from(resume, user)
    }

    @Transactional
    fun deleteResume(userId: Long, resumeId: Long) {
        val user = userService.getUser(userId)
        val resume = getResume(resumeId)

        validateResumeAuthor(resume, user)

        resumeRepository.delete(resume)
    }

    fun getResume(resumeId: Long): Resume {
        return resumeRepository.findByIdOrNull(resumeId)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_RESUME)
    }

    fun validateResumeAuthor(resume: Resume, user: User) {
        if (resume.user.id != user.id) {
            throw ErrorException(ErrorCode.INVALID_USER)
        }
    }

    fun readResume(userId: Long): ResumeReadResponse {
        val user = userService.getUser(userId)
        val resume = getResumeByUser(user)
        return ResumeReadResponse.from(resume)
    }

    fun getResumeByUser(user: User): Resume {
        return resumeRepository.findByUser(user)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_RESUME)
    }

    fun checkResumeExists(userId: Long): ResumeExistResponse {
        val hasResume: Boolean = hasResume(userId)
        return ResumeExistResponse.from(hasResume)
    }
}
