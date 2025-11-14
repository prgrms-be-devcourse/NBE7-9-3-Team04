package com.backend.api.feedback.controller;

import com.backend.api.global.JwtTest;
import com.backend.domain.answer.entity.Answer;
import com.backend.domain.answer.repository.AnswerRepository;
import com.backend.domain.feedback.entity.Feedback;
import com.backend.domain.feedback.repository.FeedbackRepository;
import com.backend.domain.question.entity.Question;
import com.backend.domain.question.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // security filter disable
@Transactional
class FeedbackControllerTest extends JwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;

    @BeforeEach
    void setUp() {
        Question question = questionRepository.findById(1L).get();

        Answer answer = new Answer(
                "프로세스는 운영체제에서 자원을 할당받아 실행되는 독립적인 실행 단위로, 각각의 프로세스는 별도의 메모리 공간을 가지며, 서로 간섭하지 않습니다. 반면, 스레드는 프로세스 내에서 실행되는 작은 실행 단위로, 같은 메모리 공간을 공유하여 자원 활용이 효율적이지만, 동기화 문제로 인해 스레드 안전성에 주의해야 합니다. 프로세스 간의 문맥 전환은 비교적 무거운 작업으로 많은 시간이 소요되나, 스레드 간의 문맥 전환은 가벼워 빠르게 이루어집니다. 또한, 프로세스는 독립적으로 실행되므로 하나의 프로세스가 종료되더라도 다른 프로세스에 영향을 미치지 않지만, 스레드는 같은 프로세스 내에서 실행되므로 하나의 스레드가 비정상 종료될 경우 전체 프로세스에 영향을 줄 수 있습니다.",
                true,
                mockUser,
                question
                );
        answerRepository.save(answer);
        Feedback feedback = Feedback.builder()
                .content("답변은 프로세스와 스레드의 기본 개념과 차이를 명확하게 설명하였으며, 메모리 공간과 자원 공유 방식, 문맥 전환 속도, 동기화 필요성 등 핵심 요소를 잘 포함하고 있습니다. 특히 스레드 안전성 문제를 언급하여 실무에서 고려해야 할 중요한 부분도 짚었다는 점에서 긍정적입니다. 다만, 프로세스의 자원뿐만 아니라, 별도의 주소 공간을 갖는다는 점과 각 프로세스가 독립적으로 실행되어 서로 간섭하지 않는다는 점, 그리고 스레드는 같은 주소 공간 내에서 실행되어 자원 공유가 가능함을 보다 명확히 구분해 주었다면 더욱 완벽했을 것입니다. 또한, 스레드 생성과 문맥 전환의 비용 비교에 대해 조금 더 구체적인 설명이 추가되면 좋겠습니다. 전체적으로 답변은 기술적으로 정확하고 면접관이 기대하는 수준에 부합합니다.")
                .aiScore(90)
                .answer(answer)
                .build();

        feedbackRepository.save(feedback);
    }

    @Nested
    @DisplayName("피드백 단건 조회 API")
    class t1 {
        @Test
        @DisplayName("정상 작동")
        void success() throws Exception {
            //given
            Question question = questionRepository.findById(1L).get();
            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/feedback/%d".formatted(question.getId()))
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(FeedbackController.class))
                    .andExpect(handler().methodName("getFeedback"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("피드백 단건 조회합니다."))
                    .andExpect(jsonPath("$.data.feedbackId").exists())
                    .andExpect(jsonPath("$.data.content").value("답변은 프로세스와 스레드의 기본 개념과 차이를 명확하게 설명하였으며, 메모리 공간과 자원 공유 방식, 문맥 전환 속도, 동기화 필요성 등 핵심 요소를 잘 포함하고 있습니다. 특히 스레드 안전성 문제를 언급하여 실무에서 고려해야 할 중요한 부분도 짚었다는 점에서 긍정적입니다. 다만, 프로세스의 자원뿐만 아니라, 별도의 주소 공간을 갖는다는 점과 각 프로세스가 독립적으로 실행되어 서로 간섭하지 않는다는 점, 그리고 스레드는 같은 주소 공간 내에서 실행되어 자원 공유가 가능함을 보다 명확히 구분해 주었다면 더욱 완벽했을 것입니다. 또한, 스레드 생성과 문맥 전환의 비용 비교에 대해 조금 더 구체적인 설명이 추가되면 좋겠습니다. 전체적으로 답변은 기술적으로 정확하고 면접관이 기대하는 수준에 부합합니다."))
                    .andExpect(jsonPath("$.data.score").value("90"))
                    .andDo(print());
        }

        @Test
        @DisplayName("피드백이 존재하지 않을 때")
        void fail1() throws Exception {
            // given
            Question question = questionRepository.findById(1L).get();
            feedbackRepository.deleteAll();
            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/feedback/%d".formatted(question.getId()))
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(FeedbackController.class))
                    .andExpect(handler().methodName("getFeedback"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("피드백을 찾을 수 없습니다."))
                    .andDo(print());
        }
    }
}