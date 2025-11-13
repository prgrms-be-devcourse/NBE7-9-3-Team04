package com.backend.api.review.service;

import com.backend.api.question.service.AiQuestionService;
import com.backend.api.resume.service.ResumeService;
import com.backend.api.review.dto.request.AiReviewbackRequest;
import com.backend.api.review.dto.response.AiReviewResponse;
import com.backend.domain.resume.entity.Resume;
import com.backend.domain.review.entity.Review;
import com.backend.domain.review.repository.ReviewRepository;
import com.backend.domain.user.entity.User;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AiReviewService {

    private final ReviewRepository reviewRepository;
    private final ResumeService resumeService;
    private final AiQuestionService aiQuestionService;

    @Transactional
    public AiReviewResponse createAiReview(User user) throws JsonProcessingException {
        if (user == null) {
            throw new ErrorException(ErrorCode.UNAUTHORIZED_USER);
        }

        if (!user.isPremium()) {
            throw new ErrorException(ErrorCode.AI_FEEDBACK_FOR_PREMIUM_ONLY);
        }

        Resume resume = resumeService.getResumeByUser(user);
        AiReviewbackRequest request = AiReviewbackRequest.of(resume);
        String feedbackContent = aiQuestionService.getAiReviewContent(request);

        Review reviewEntity = Review.builder()
                .user(user)
                .resume(resume)
                .AiReviewContent(feedbackContent)
                .build();

        reviewRepository.save(reviewEntity);

        return AiReviewResponse.of(reviewEntity.getId(), feedbackContent, reviewEntity.getCreateDate());
    }

    public AiReviewResponse findReviewById(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_REVIEW));

        if (!Objects.equals(review.getUser().getId(), user.getId())) {
            throw new ErrorException(ErrorCode.ACCESS_DENIED_REVIEW);
        }

        return AiReviewResponse.of(review.getId(), review.getAiReviewContent(), review.getCreateDate());
    }

    public List<AiReviewResponse> findMyAiReviews(User user) {
        List<Review> reviews = reviewRepository.findByUserIdOrderByIdDesc(user.getId());

        return reviews.stream()
                .map(review -> AiReviewResponse.of(review.getId(), review.getAiReviewContent(), review.getCreateDate()))
                .collect(Collectors.toList());
    }
}