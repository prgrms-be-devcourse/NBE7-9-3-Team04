package com.backend.api.question.controller;

import com.backend.api.global.JwtTest;
import com.backend.api.question.dto.response.AiQuestionReadAllResponse;
import com.backend.domain.question.entity.Question;
import com.backend.domain.question.entity.QuestionCategoryType;
import com.backend.domain.question.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // security filter disable
@Transactional
class AiQuestionControllerTest extends JwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionRepository questionRepository;

    private Long questionId;
    private UUID groupId;
    @BeforeEach
    void setUp() {
        UUID uuid = UUID.randomUUID();
        Question question1 = Question.builder()
                .title("기존 제목")
                .content("기존 내용1")
                .author(mockUser)
                .categoryType(QuestionCategoryType.PORTFOLIO)
                .groupId(uuid)
                .build();
        question1 = questionRepository.save(question1);
        questionId = question1.getId();
        groupId = question1.getGroupId();

        Question question2 = Question.builder()
                .title("기존 제목")
                .content("기존 내용2")
                .author(mockUser)
                .categoryType(QuestionCategoryType.PORTFOLIO)
                .groupId(uuid)
                .build();
        questionRepository.save(question2);

        Question question3 = Question.builder()
                .title("기존 제목")
                .content("기존 내용3")
                .author(mockUser)
                .categoryType(QuestionCategoryType.PORTFOLIO)
                .groupId(uuid)
                .build();
        questionRepository.save(question3);

        Question question4 = Question.builder()
                .title("기존 제목")
                .content("기존 내용4")
                .author(mockUser)
                .categoryType(QuestionCategoryType.PORTFOLIO)
                .groupId(uuid)
                .build();
        questionRepository.save(question4);

        Question question5 = Question.builder()
                .title("기존 제목")
                .content("기존 내용5")
                .author(mockUser)
                .categoryType(QuestionCategoryType.PORTFOLIO)
                .groupId(uuid)
                .build();
        questionRepository.save(question5);
    }

    @Nested
    @DisplayName("AI 면접 질문 조회 API")
    class t1 {
        @Test
        @DisplayName("정상 작동")
        void success() throws Exception {
            //given
            Question question = questionRepository.findById(questionId).get();
            AiQuestionReadAllResponse response = questionRepository.getQuestionByCategoryTypeAndUserId(QuestionCategoryType.PORTFOLIO, mockUser).orElseGet(null);
            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/ai/questions")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(AiQuestionController.class))
                    .andExpect(handler().methodName("readAllAiQuestion"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("AI 면접 질문 목록이 조회되었습니다."))
                    .andExpect(jsonPath("$.data.questions[0].groupId").value(groupId.toString()))
                    .andExpect(jsonPath("$.data.questions[0].title").value(question.getTitle()))
                    .andExpect(jsonPath("$.data.questions[0].count").value(response.questions().get(0).count()))
                    .andDo(print());
        }

        @Test
        @DisplayName("유저가 존재하지 않을 때")
        void fail1() throws Exception {
            // given
            userRepository.deleteAll();
            // when
            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/ai/questions")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(AiQuestionController.class))
                    .andExpect(handler().methodName("readAllAiQuestion"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                    .andDo(print());
        }

    }

    @Nested
    @DisplayName("그룹별 AI 면접 질문 조회 API")
    class t2 {
        @Test
        @DisplayName("정상 작동")
        void success() throws Exception {
            //given
            Question question = questionRepository.findById(questionId).get();
            AiQuestionReadAllResponse response = questionRepository.getQuestionByCategoryTypeAndUserId(QuestionCategoryType.PORTFOLIO, mockUser).orElseGet(null);
            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/ai/questions/%s".formatted(groupId))
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(AiQuestionController.class))
                    .andExpect(handler().methodName("readAiQuestion"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("그룹별 AI 면접 질문 목록이 조회되었습니다."))
                    .andExpect(jsonPath("$.data.title").value(question.getTitle()))
                    .andExpect(jsonPath("$.data.count").value(response.questions().get(0).count()))
                    .andExpect(jsonPath("$.data.questions[0].id").value(question.getId()))
                    .andExpect(jsonPath("$.data.questions[0].content").value(question.getContent()))
                    .andDo(print());
        }

        @Test
        @DisplayName("유저가 존재하지 않을 때")
        void fail1() throws Exception {
            // given
            userRepository.deleteAll();
            // when
            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/ai/questions/%s".formatted(groupId))
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(AiQuestionController.class))
                    .andExpect(handler().methodName("readAiQuestion"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                    .andDo(print());
        }

    }
}