package com.backend.api.user.service

import com.backend.api.user.dto.request.MyPageRequest
import com.backend.api.user.dto.response.UserMyPageResponse
import com.backend.domain.user.repository.UserRepository
import com.backend.domain.userQuestion.entity.UserQuestion
import com.backend.domain.userQuestion.repository.UserQuestionRepository
import com.backend.global.Rq.Rq
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
@Transactional
class UserMyPageService(private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder,
        private val rq: Rq,
        private val userQuestionRepository: UserQuestionRepository) {

    fun modifyUser(userId: Long, modify: MyPageRequest.UserModify): UserMyPageResponse {
        val user = userRepository.findById(userId)
            .orElseThrow{ IllegalArgumentException("유저를 찾을 수 없습니다.") }

        val encodedPassword = when {
            modify.password.isNullOrBlank() -> user.password
            else -> passwordEncoder.encode(modify.password)
        }

        user.updateUser(
            email = modify.email ?: user.email,
            password = encodedPassword,
            name = modify.name ?: user.name,
            nickname = modify.nickname ?: user.nickname,
            age = modify.age ?: user.age,
            github = modify.github ?: user.github,
            image = modify.image ?: user.image
        )
        if (rq.getUser().id != userId) {
            throw ErrorException(ErrorCode.SELF_INFORMATION)
        }

        val saved = userRepository.save(user)
        return UserMyPageResponse.fromEntity(saved)
    }

    fun getInformation(userId: Long): UserMyPageResponse {
        val users = userRepository.findById(userId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_USER) }

        return UserMyPageResponse.fromEntity(users)
    }


    fun getSolvedProblems(userId: Long): List<UserMyPageResponse.SolvedProblem> {
        val solvedQuestions: List<UserQuestion> =
            userQuestionRepository.findByUser_Id(userId)

        return solvedQuestions.map { q ->
            UserMyPageResponse.SolvedProblem(
                title = q.question.title,
                modifyDate = q.modifyDate
            )
        }
    }

    fun verifyPassword(userId: Long, rawPassword: String?): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow{ ErrorException(ErrorCode.NOT_FOUND_USER) }

        return passwordEncoder.matches(rawPassword, user.password)
    }
}
