package com.backend.api.answer.service;

import com.backend.api.answer.dto.request.AnswerCreateRequest;
import com.backend.api.answer.dto.request.AnswerUpdateRequest;
import com.backend.api.answer.dto.response.AnswerMypageResponse;
import com.backend.api.answer.dto.response.AnswerPageResponse;
import com.backend.api.answer.dto.response.AnswerReadResponse;
import com.backend.api.answer.dto.response.AnswerReadWithScoreResponse;
import com.backend.api.feedback.event.publisher.FeedbackCreateEvent;
import com.backend.api.feedback.event.publisher.FeedbackPublisher;
import com.backend.api.feedback.event.publisher.FeedbackUpdateEvent;
import com.backend.api.question.service.QuestionService;
import com.backend.api.user.service.UserService;
import com.backend.domain.answer.entity.Answer;
import com.backend.domain.answer.repository.AnswerRepository;
import com.backend.domain.question.entity.Question;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;
import com.backend.global.Rq.Rq;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionService questionService;
    private final Rq rq;
    private final UserService userService;

    private final FeedbackPublisher feedbackPublisher;

    public Answer findByIdOrThrow(Long id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new ErrorException(ErrorCode.ANSWER_NOT_FOUND));
    }

    @Transactional
    public Answer writeAnswer(User currentUser, Long questionId, AnswerCreateRequest reqBody) {
        String content = reqBody.content();

        Question question = questionService.findByIdOrThrow(questionId);
        Boolean isPublic = reqBody.isPublic();

        Answer answer = Answer.builder()
                .content(content)
                .isPublic(isPublic)
                .author(currentUser)
                .question(question)
                .build();
        Answer savedAnswer = answerRepository.save(answer);
        feedbackPublisher.publishFeedbackCreate(answer);

        return savedAnswer;
    }

    @Transactional
    public Answer updateAnswer(User currentUser, Long answerId, AnswerUpdateRequest reqBody) {
        Answer answer = this.findByIdOrThrow(answerId);

        if (!answer.getAuthor().getId().equals(currentUser.getId())) {
            throw new ErrorException(ErrorCode.ANSWER_INVALID_USER);
        }

        answer.update(reqBody.content(), reqBody.isPublic());
        feedbackPublisher.publishFeedbackUpdate(answer);
        return answer;
    }

    @Transactional
    public void deleteAnswer(User currentUser, Long answerId) {
        Answer answer = this.findByIdOrThrow(answerId);

        if (!answer.getAuthor().getId().equals(currentUser.getId())) {
            throw new ErrorException(ErrorCode.ANSWER_INVALID_USER);
        }

        answerRepository.delete(answer);
    }

    public AnswerPageResponse<AnswerReadWithScoreResponse> findAnswersByQuestionId(int page, Long questionId) {
        questionService.findByIdOrThrow(questionId);

        if(page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<Answer> answersPage = answerRepository.findByQuestionIdAndIsPublicTrueOrderByFeedbackScoreDesc(questionId, pageable);

        List<AnswerReadWithScoreResponse> answers = answersPage.getContent()
                .stream()
                .map(answer -> {
                    Integer score = answer.getFeedback() != null ? answer.getFeedback().getAiScore() : 0;
                    return new AnswerReadWithScoreResponse(answer, score);
                })
                .toList();

        return new AnswerPageResponse<>(answersPage, answers);
    }

    public AnswerReadResponse findMyAnswer(Long questionId) {
        // 질문 존재 여부 확인
        questionService.findByIdOrThrow(questionId);

        User currentUser = rq.getUser();

        // 질문에 대해 현재 사용자가 작성한 답변 조회
        return answerRepository.findFirstByQuestionIdAndAuthorId(questionId, currentUser.getId())
                .map(AnswerReadResponse::new) // 있으면 DTO로 변환
                .orElse(null); // 없으면 null 반환
    }

    public AnswerPageResponse<AnswerMypageResponse> findAnswersByUserId(int page, Long userId) {
        userService.getUser(userId);
        User currentUser = rq.getUser();
        if(!currentUser.getId().equals(userId) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new ErrorException(ErrorCode.ANSWER_INVALID_USER);
        }

        if(page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, 15, Sort.by("createDate").descending());
        Page<Answer> answersPage = answerRepository.findByAuthorId(userId, pageable);

        List<AnswerMypageResponse> answers = answersPage
                .getContent()
                .stream()
                .map(AnswerMypageResponse::new)
                .toList();

        return new AnswerPageResponse<>(answersPage, answers);
    }

}
