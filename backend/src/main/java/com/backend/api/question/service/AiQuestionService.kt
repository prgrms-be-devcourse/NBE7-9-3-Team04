package com.backend.api.question.service;

import com.backend.global.ai.handler.AiRequestHandler;
import com.backend.api.question.dto.request.AiQuestionRequest;
import com.backend.api.question.dto.response.*;

import com.backend.api.resume.service.ResumeService;
import com.backend.api.review.dto.request.AiReviewbackRequest;
import com.backend.api.user.service.UserService;
import com.backend.domain.question.entity.Question;
import com.backend.domain.question.entity.QuestionCategoryType;
import com.backend.domain.resume.entity.Resume;
import com.backend.domain.user.entity.User;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiQuestionService {

    private final QuestionService questionService;
    private final UserService userService;
    private final ResumeService resumeService;
    private final ObjectMapper objectMapper;
    private final AiRequestHandler aiRequestHandler;

    public AIQuestionCreateResponse createAiQuestion(Long userId) throws JsonProcessingException {

        User user = userService.getUser(userId);
        // AI 질문 생성 횟수 제한 검증
        validateQuestionLimit(user);

        Resume resume = resumeService.getResumeByUser(user);
        AiQuestionRequest request = AiQuestionRequest.of(resume.getSkill(), resume.getPortfolioUrl(), user.getAiQuestionLimit());

        String connectionAi = aiRequestHandler.connectionAi(request);

        List<AiQuestionResponse> responses = parseChatGptResponse(connectionAi);

        List<Question> questions = listDtoToEntity(responses, user, resume);

        questionService.createListQuestion(questions);
        List<AiQuestionResponse> questionDto = AiQuestionResponse.toDtoList(questions);
        return AIQuestionCreateResponse.from(questions.getFirst().getGroupId(),questionDto);
    }

    @Transactional(readOnly = true)
    public String getAiReviewContent(AiReviewbackRequest request) throws JsonProcessingException {
        String rawApiResponse = aiRequestHandler.connectionAi(request);
        return parseSingleContentFromResponse(rawApiResponse);
    }

    // AI 질문 생성 횟수 제한 검증
    private void validateQuestionLimit(User user) {
        int availableCount = user.getAiQuestionLimit();
        int usedCount = user.getAiQuestionUsedCount();

        if (usedCount >= availableCount) {
            throw new ErrorException(ErrorCode.AI_QUESTION_LIMIT_EXCEEDED);
        }
        user.incrementAiQuestionUsedCount();
    }

    private List<AiQuestionResponse> parseChatGptResponse(String connectionAi) throws JsonProcessingException {
        ChatGptResponse responseDto = objectMapper.readValue(connectionAi, ChatGptResponse.class);

        String content = responseDto.choiceResponses().getFirst().message().content();

        String cleanJson = content
                .replaceAll("```json", "")
                .replaceAll("\\n","")
                .trim();

        return objectMapper.readValue(cleanJson, new TypeReference<>() {});
    }


    private String parseSingleContentFromResponse(String rawApiResponse) throws JsonProcessingException {
        ChatGptResponse responseDto = objectMapper.readValue(rawApiResponse, ChatGptResponse.class);
        return responseDto.choiceResponses().getFirst().message().content();
    }


    public List<Question> listDtoToEntity(List<AiQuestionResponse> responses, User user, Resume resume){
        UUID groupId = UUID.randomUUID();
        return responses.stream()
                .map(dto -> Question.builder()
                        .title(resume.getPortfolioUrl())
                        .content(Optional.ofNullable(dto.content())
                                .filter(s -> !s.isBlank())
                                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_CONTENT)))
                        .score(2)
                        .isApproved(true)
                        .author(user)
                        .groupId(groupId)
                        .categoryType(QuestionCategoryType.PORTFOLIO)
                        .build()
                )
                .toList();
    }


    @Transactional(readOnly = true)
    public AiQuestionReadAllResponse readAllAiQuestion(Long userId) {
        User user = userService.getUser(userId);
        return questionService.getByCategoryType( QuestionCategoryType.PORTFOLIO, user);
    }

    @Transactional(readOnly = true)
    public PortfolioListReadResponse readAiQuestion(Long userId, UUID groupId) {
        User user = userService.getUser(userId);
        return questionService.getByUserAndGroupId(user, groupId);
    }
}
