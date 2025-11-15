package com.backend.api.feedback.service;



import com.backend.api.answer.dto.response.AnswerReadResponse;
import com.backend.api.answer.service.AnswerService;
import com.backend.api.feedback.dto.response.AiFeedbackResponse;
import com.backend.api.feedback.dto.request.AiFeedbackRequest;
import com.backend.api.feedback.dto.response.FeedbackReadResponse;
import com.backend.api.question.service.QuestionService;
import com.backend.api.ranking.service.RankingService;
import com.backend.api.userQuestion.service.UserQuestionService;
import com.backend.domain.answer.entity.Answer;
import com.backend.domain.feedback.entity.Feedback;
import com.backend.domain.feedback.repository.FeedbackRepository;
import com.backend.domain.question.entity.Question;
import com.backend.domain.user.entity.User;
import com.backend.global.ai.handler.AiRequestHandler;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {


    private final FeedbackRepository feedbackRepository;
    private final QuestionService questionService;
    private final AiRequestHandler aiRequestHandler;
    private final AnswerService answerService;
    private final UserQuestionService userQuestionService;
    private final RankingService rankingService;
    private final VertexAiGeminiChatModel geminiModel;

    @Transactional
    public void createFeedback(Answer answer){
        Question question = questionService.findByIdOrThrow(answer.getQuestion().getId());
        AiFeedbackResponse aiFeedback = createAiFeedback(question, answer);

        Feedback feedback = Feedback.builder()
                .answer(answer)
                .aiScore(aiFeedback.score())
                .content(aiFeedback.content())
                .build();

        feedbackRepository.save(feedback);

        int baseScore = question.getScore();
        double ratio = feedback.getAiScore() / 100.0;
        int finalScore = (int)Math.round(ratio * baseScore);

        userQuestionService.updateUserQuestionScore(answer.getAuthor(), question, finalScore);
        rankingService.updateUserRanking(answer.getAuthor());
    }

    @Transactional
    public void updateFeedback(Answer answer){
        Question question = questionService.findByIdOrThrow(answer.getQuestion().getId());
        AiFeedbackResponse aiFeedback = createAiFeedback(question, answer);
        Feedback feedback = getFeedbackByAnswerId(answer.getId());
        feedback.update(answer,aiFeedback.score(),aiFeedback.content());

        int baseScore = question.getScore();
        double ratio = feedback.getAiScore() / 100.0;
        int finalScore = (int)Math.round(ratio * baseScore);

        userQuestionService.updateUserQuestionScore(answer.getAuthor(), question, finalScore);
        rankingService.updateUserRanking(answer.getAuthor());
    }

    public Feedback getFeedbackByAnswerId(Long answerId){
        return feedbackRepository.findByAnswerId(answerId)
                .orElseThrow(() -> new ErrorException(ErrorCode.FEEDBACK_NOT_FOUND));
    }


    public AiFeedbackResponse createAiFeedback(Question question, Answer answer){

        // 리퀘스트 dto 정의
        AiFeedbackRequest request = AiFeedbackRequest.of(question.getContent(),answer.getContent());

        Prompt prompt = createPrompt(request.systemMessage(), request.userMessage(), request.assistantMessage());

        return connectChatClient(prompt);
    }


    @Retry(name = "aiRetry")
    @CircuitBreaker(name = "aiExternal", fallbackMethod = "fallbackToGemini")
    public AiFeedbackResponse connectChatClient(Prompt prompt){
        return aiRequestHandler.execute(client ->
                client.prompt(prompt)
                        .call()
                        .entity(AiFeedbackResponse.class)
        );
    }

    public AiFeedbackResponse fallbackToGemini(Prompt prompt, Throwable e ) {
        ChatClient geminiClient = ChatClient.create(geminiModel);
        log.warn("[Fallback Triggered] OpenAI unavailable → switching to Gemini. Cause={}", e.getMessage());
        return geminiClient.prompt(prompt)
                .call()
                .entity(AiFeedbackResponse.class);
    }

    private Prompt createPrompt(String system, String user, String assistant){
        // role별 메시지
        SystemMessage systemMessage = new SystemMessage(system);
        UserMessage userMessage = new UserMessage(user);
        AssistantMessage assistantMessage = new AssistantMessage(assistant);

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(1.0)
                .build();

        // 프롬프트
        return new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);
    }


    @Transactional(readOnly = true)
    public FeedbackReadResponse readFeedback(Long questionId,User user) {
        AnswerReadResponse response = answerService.findMyAnswer(questionId);
        Feedback feedback = getFeedbackByAnswerId(response.id());

        return FeedbackReadResponse.from(feedback);
    }

    public Feedback getFeedback(Long id){
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ErrorException(ErrorCode.FEEDBACK_NOT_FOUND));
    }
}
