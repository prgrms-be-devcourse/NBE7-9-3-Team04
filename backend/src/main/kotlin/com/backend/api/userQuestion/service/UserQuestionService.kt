package com.backend.api.userQuestion.service

import com.backend.domain.question.entity.Question
import com.backend.domain.user.entity.User
import com.backend.domain.userQuestion.entity.UserQuestion
import com.backend.domain.userQuestion.repository.UserQuestionRepository
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserQuestionService(
    private val userQuestionRepository: UserQuestionRepository
) {
    @Transactional
    fun createUserQuestion(user: User, question: Question): UserQuestion {

        if (userQuestionRepository.existsByUserAndQuestion(user, question)) {
            throw ErrorException(ErrorCode.USER_QUESTION_ALREADY_EXISTS)
        }

        val userQuestion = UserQuestion(
            user = user,
            question = question,
            aiScore = 0
        )

        return userQuestionRepository.save(userQuestion)
    }

    @Transactional
    fun updateUserQuestionScore(user: User, question: Question, aiScore: Int?) {
        if (aiScore == null) return

        val userQuestion = userQuestionRepository.findByUserAndQuestion(user, question)
            ?: createUserQuestion(user, question)

        userQuestion.updateAiScoreIfHigher(aiScore)

        userQuestionRepository.save(userQuestion)
    }

    @Transactional(readOnly = true)
    fun getTotalUserQuestionScore(user: User): Int {
        return userQuestionRepository.sumAiScoreByUser(user) ?: 0
    }

    @Transactional(readOnly = true)
    fun countSolvedQuestion(user: User): Int {
        return userQuestionRepository.countByUser(user)
    }
}
