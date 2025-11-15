package com.backend.api.question.service;

import com.backend.api.question.dto.request.QuestionAddRequest;
import com.backend.api.question.dto.request.QuestionUpdateRequest;
import com.backend.api.question.dto.response.*;
import com.backend.api.user.service.UserService;
import com.backend.domain.question.entity.Question;
import com.backend.domain.question.entity.QuestionCategoryType;
import com.backend.domain.question.repository.QuestionRepository;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;
import com.backend.global.Rq.Rq;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserService userService;
    private final Rq rq;

    //사용자 권한 검증
    private void validateUserAuthority(User user) {
        if (user == null) {
            throw new ErrorException(ErrorCode.UNAUTHORIZED_USER);
        }
        if (user.getRole() != Role.USER) {
            throw new ErrorException(ErrorCode.FORBIDDEN);
        }
    }

    //질문 존재 여부 검증
    public Question findByIdOrThrow(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_QUESTION));
    }

    //질문 작성자 본인 여부 검증
    private void validateQuestionAuthor(Question question, User user) {
        if (question.getAuthor().getId() != user.getId()) {
            throw new ErrorException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateApprovedQuestion(Question question) {
        if (!question.getIsApproved()) {
            throw new ErrorException(ErrorCode.QUESTION_NOT_APPROVED);
        }
    }

    //사용자 질문 생성
    @Transactional
    public QuestionResponse addQuestion(@Valid QuestionAddRequest request, User user) {
        validateUserAuthority(user);
        Question question = createQuestion(request, user);
        Question saved = saveQuestion(question);
        return QuestionResponse.from(saved);
    }

    private Question createQuestion(QuestionAddRequest request, User user) {
        return Question.builder()
                .title(request.title())
                .content(request.content())
                .author(user)
                .categoryType(request.categoryType())
                .build();
    }


    private Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }

    //사용자 질문 수정
    @Transactional
    public QuestionResponse updateQuestion(Long questionId, @Valid QuestionUpdateRequest request, User user) {
        validateUserAuthority(user);
        Question question = findByIdOrThrow(questionId);
        validateQuestionAuthor(question, user);
        updateQuestionContent(question, request);
        return QuestionResponse.from(question);
    }

    private void updateQuestionContent(Question question, QuestionUpdateRequest request) {
        question.updateUserQuestion(
                request.title(),
                request.content(),
                request.categoryType()
        );
    }

    //승인된 질문 전체 조회
    @Transactional(readOnly = true)
    public QuestionPageResponse<QuestionResponse> getApprovedQuestions(int page, QuestionCategoryType categoryType) {
        Page<Question> questionsPage;

        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, 9, Sort.by("createDate").descending());

        if (categoryType == null) {
            questionsPage = questionRepository.findApprovedQuestionsExcludingCategory(QuestionCategoryType.PORTFOLIO, pageable);
        }
        else if (categoryType == QuestionCategoryType.PORTFOLIO) {
            // 포트폴리오만 조회
            questionsPage = questionRepository.findApprovedQuestionsByCategory(QuestionCategoryType.PORTFOLIO, pageable);
        }
        else {
            questionsPage = questionRepository.findByCategoryTypeAndIsApprovedTrue(categoryType, pageable);
        }

        if (questionsPage.isEmpty()) {
            throw new ErrorException(ErrorCode.NOT_FOUND_QUESTION);
        }

        List<QuestionResponse> questions = mapToResponseList(questionsPage);

        return QuestionPageResponse.from(questionsPage, questions);

    }

    private List<QuestionResponse> mapToResponseList(Page<Question> questionsPage) {
        return questionsPage.getContent()
                .stream()
                .map(QuestionResponse::from)
                .toList();
    }

    //승인된 질문 단건 조회
    @Transactional(readOnly = true)
    public QuestionResponse getApprovedQuestionById(Long questionId) {
        Question question = findByIdOrThrow(questionId);
        validateApprovedQuestion(question);
        return QuestionResponse.from(question);
    }

    //승인되지 않은 질문 단건 조회 -> 수정용
    @Transactional(readOnly = true)
    public QuestionResponse getNotApprovedQuestionById(Long userId, Long questionId, User user) {
        validateUserAuthority(user);

        if (user.getId() != userId) {
            throw new ErrorException(ErrorCode.QUESTION_INVALID_USER);
        }
        Question question = findByIdOrThrow(questionId);
        validateQuestionAuthor(question, user);

        if (question.getIsApproved()) {
            throw new ErrorException(ErrorCode.ALREADY_APPROVED_QUESTION);
        }

        return QuestionResponse.from(question);
    }

    @Transactional
    public void createListQuestion(List<Question> questions){
        questionRepository.saveAll(questions);
    }


    public AiQuestionReadAllResponse getByCategoryType(QuestionCategoryType questionCategoryType, User user) {
        return questionRepository.getQuestionByCategoryTypeAndUserId(questionCategoryType,user)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_QUESTION));
    }

    public PortfolioListReadResponse getByUserAndGroupId(User user, UUID groupId) {
        return questionRepository.getByUserAndGroupId(user, groupId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_QUESTION));
    }

    @Transactional(readOnly = true)
    public int countByUser(User user) {
        validateUserAuthority(user);
        return questionRepository.countByAuthor(user);
    }

    @Transactional(readOnly = true)
    public QuestionPageResponse<QuestionResponse> findQuestionsByUserId(int page, Long userId) {
        userService.getUser(userId);
        User currentUser = rq.getUser();

        if (currentUser.getId() != userId && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new ErrorException(ErrorCode.QUESTION_INVALID_USER);
        }

        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, 15, Sort.by("createDate").descending());

        Page<Question> questionsPage = questionRepository.findByAuthorId(userId, pageable);

        List<QuestionResponse> questions = questionsPage
                .getContent()
                .stream()
                .map(QuestionResponse::from)
                .toList();

        return QuestionPageResponse.from(questionsPage, questions);
    }
}
