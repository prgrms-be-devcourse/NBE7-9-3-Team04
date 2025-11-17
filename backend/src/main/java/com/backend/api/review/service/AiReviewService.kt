package com.backend.api.review.service

import com.backend.api.question.service.AiQuestionService
import com.backend.api.resume.service.ResumeService
import com.backend.api.review.dto.request.AiReviewbackRequest
import com.backend.api.review.dto.response.AiReviewResponse
import com.backend.domain.review.entity.Review
import com.backend.domain.review.repository.ReviewRepository
import com.backend.domain.user.entity.User
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AiReviewService(
    private val reviewRepository: ReviewRepository,
    private val resumeService: ResumeService,
    private val aiQuestionService: AiQuestionService
) {

    @Transactional
    fun createAiReview(user: User): AiReviewResponse {

        if (!user.isPremium()) {
            ErrorException(ErrorCode.AI_FEEDBACK_FOR_PREMIUM_ONLY)
        }

        val resume = resumeService.getResumeByUser(user)
        val request = AiReviewbackRequest.of(resume)
        val feedbackContent = aiQuestionService.getAiReviewContent(request)

        val reviewEntity = Review(
            user = user,
            resume = resume,
            AiReviewContent = feedbackContent
        )

        reviewRepository.save(reviewEntity)


        return AiReviewResponse.of(reviewEntity.id, feedbackContent, reviewEntity.createDate)
    }

    fun findReviewById(reviewId: Long, user: User): AiReviewResponse {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_REVIEW) }

        if (review.user?.id != user.id) {
            ErrorException(ErrorCode.ACCESS_DENIED_REVIEW)
        }

        return AiReviewResponse.of(review.id, review.AiReviewContent, review.createDate)
    }

    fun findMyAiReviews(user: User?): List<AiReviewResponse> {
        if (user == null) {
         throw ErrorException(ErrorCode.UNAUTHORIZED_USER)
        }


        val reviews: List<Review> = reviewRepository.findByUserIdOrderByIdDesc(user.id)


        return reviews
            .map { review ->
                AiReviewResponse.of(
                    review.id,
                    review.AiReviewContent,
                    review.createDate
                )
            }
            .toList()
    }
}