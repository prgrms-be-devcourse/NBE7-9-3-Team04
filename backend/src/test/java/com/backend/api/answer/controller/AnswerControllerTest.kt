package com.backend.api.answer.controller;

import com.backend.api.global.JwtTest;
import com.backend.domain.answer.repository.AnswerRepository;
import com.backend.domain.answer.entity.Answer;
import com.backend.domain.question.entity.Question;
import com.backend.domain.question.repository.QuestionRepository;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AnswerControllerTest extends JwtTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerRepository answerRepository;

    private Long questionId;
    private List<Long> answerIdList;
    private List<Long> userIdList;

    // 테스트 데이터 셋업
    @BeforeEach
    @Transactional
    void setUp() {

        User generalUser = User.builder()
                .email("general@user.com")
                .password("asdf1234!")
                .name("홍길동")
                .nickname("gildong")
                .age(20)
                .github("abc123")
                .image(null)
                .role(Role.USER)
                .build();

        User generalUser2 = User.builder()
                .email("general2@user.com")
                .password("asdf1234!")
                .name("홍길똥")
                .nickname("gilddong")
                .age(25)
                .github("abc1233")
                .image(null)
                .role(Role.USER)
                .build();

        userRepository.save(generalUser);
        userRepository.save(generalUser2);
        userIdList = Arrays.asList(generalUser.getId(), generalUser2.getId());

        Question question1 = Question.builder()
                .title("첫 번째 질문 제목")
                .content("첫 번째 질문 내용")
                .author(mockUser)
                .build();
        questionRepository.save(question1);
        questionId = question1.getId();

        Answer answer1 = Answer.builder()
                .content("첫 번째 답변 내용")
                .isPublic(true)
                .author(mockUser)
                .question(question1)
                .build();

        Answer answer2 = Answer.builder()
                .content("두 번째 답변 내용")
                .isPublic(false)
                .author(userRepository.findById(userIdList.get(0)).orElseThrow())
                .question(question1)
                .build();

        Answer answer3 = Answer.builder()
                .content("세 번째 답변 내용")
                .isPublic(true)
                .author(userRepository.findById(userIdList.get(0)).orElseThrow())
                .question(question1)
                .build();

        Answer answer4 = Answer.builder()
                .content("네 번째 답변 내용")
                .isPublic(true)
                .author(mockUser)
                .question(question1)
                .build();

        Answer answer5 = Answer.builder()
                .content("다섯 번째 답변 내용")
                .isPublic(true)
                .author(mockUser)
                .question(question1)
                .build();

        Answer answer6 = Answer.builder()
                .content("여섯 번째 답변 내용")
                .isPublic(true)
                .author(mockUser)
                .question(question1)
                .build();

        Answer answer7 = Answer.builder()
                .content("일곱 번째 답변 내용")
                .isPublic(true)
                .author(mockUser)
                .question(question1)
                .build();

        Answer answer8 = Answer.builder()
                .content("여덟 번째 답변 내용")
                .isPublic(true)
                .author(mockUser)
                .question(question1)
                .build();
        Answer answer9 = Answer.builder()
                .content("아홉 번째 답변 내용")
                .isPublic(true)
                .author(mockUser)
                .question(question1)
                .build();

        Answer answer10 = Answer.builder()
                .content("열 번째 답변 내용")
                .isPublic(true)
                .author(mockUser)
                .question(question1)
                .build();

        Answer answer11 = Answer.builder()
                .content("열한 번째 답변 내용")
                .isPublic(true)
                .author(mockUser)
                .question(question1)
                .build();

        Answer answer12 = Answer.builder()
                .content("열두 번째 답변 내용")
                .isPublic(true)
                .author(mockUser)
                .question(question1)
                .build();

        Answer answer13 = Answer.builder()
                .content("열세 번째 답변 내용")
                .isPublic(true)
                .author(userRepository.findById(userIdList.get(0)).orElseThrow())
                .question(question1)
                .build();

        answerRepository.save(answer1);
        answerRepository.save(answer2);
        answerRepository.save(answer3);
        answerRepository.save(answer4);
        answerRepository.save(answer5);
        answerRepository.save(answer6);
        answerRepository.save(answer7);
        answerRepository.save(answer8);
        answerRepository.save(answer9);
        answerRepository.save(answer10);
        answerRepository.save(answer11);
        answerRepository.save(answer12);
        answerRepository.save(answer13);
        answerIdList = Arrays.asList(
                answer1.getId(), answer2.getId(), answer3.getId(), answer4.getId(), answer5.getId(),
                answer6.getId(), answer7.getId(), answer8.getId(), answer9.getId(), answer10.getId(),
                answer11.getId(), answer12.getId(), answer13.getId()
        );
    }

    @Nested
    @DisplayName("답변 생성 테스트")
    class t1 {

        @Test
        @DisplayName("1번 질문에 생성 성공")
        void success() throws Exception {
            long targetQuestionId = questionId;
            String answer = "새로운 답변 내용입니다.";

            // DB에 답변 생성 전 개수
            long initialAnswerCount = answerRepository.count();

            ResultActions resultActions = mvc
                    .perform(
                            post("/api/v1/questions/%d/answers".formatted(targetQuestionId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                        {
                                            "content": "%s",
                                            "isPublic": true
                                        }
                                        """.formatted(answer))
                    )
                    .andDo(print());

            // DB에 답변 생성 후 개수 확인
            List<Answer> newAnswers = answerRepository.findAll();
            assertThat(newAnswers.size()).isEqualTo(initialAnswerCount + 1);

            // 생성된 답변 정보 동적 확인
            Answer createdAnswer = newAnswers.stream()
                    .filter(c -> c.getContent().equals(answer))
                    .filter(c -> c.getQuestion().getId().equals(targetQuestionId))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("새로 생성된 답변을 찾을 수 없습니다."));

            long createdAnswerId = createdAnswer.getId();

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("createAnswer"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$.message").value("%d번 답변이 생성되었습니다.".formatted(createdAnswerId)))
                    .andExpect(jsonPath("$.data.id").value(createdAnswerId))
                    .andExpect(jsonPath("$.data.createDate").exists())
                    .andExpect(jsonPath("$.data.modifyDate").exists())
                    .andExpect(jsonPath("$.data.content").value(answer))
                    .andExpect(jsonPath("$.data.isPublic").value(true))
//                    .andExpect(jsonPath("$.data.feedback").value(Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.authorId").value(createdAnswer.getAuthor().getId()))
                    .andExpect(jsonPath("$.data.authorNickName").value(createdAnswer.getAuthor().getNickname()))
                    .andExpect(jsonPath("$.data.questionId").value(targetQuestionId));
        }

        @Test
        @DisplayName("필수 필드 누락")
        void fail1() throws Exception {
            long targetQuestionId = questionId;

            ResultActions resultActions = mvc
                    .perform(
                            post("/api/v1/questions/%d/answers".formatted(targetQuestionId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                        {
                                            "isPublic": true
                                        }
                                        """.formatted())
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("createAnswer"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("답변 내용을 입력해주세요."));
        }

        @Test
        @DisplayName("필수 필드 누락 2")
        void fail1_2() throws Exception {
            long targetQuestionId = questionId;
            String answer = "새로운 답변 내용입니다.";

            ResultActions resultActions = mvc
                    .perform(
                            post("/api/v1/questions/%d/answers".formatted(targetQuestionId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(answer))
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("createAnswer"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("답변 공개 여부를 선택해주세요."));
        }

        @Test
        @DisplayName("존재하지 않는 질문에 답변 생성 시도")
        void fail2() throws Exception {
            long targetQuestionId = 9999;
            String answer = "새로운 답변 내용입니다.";

            ResultActions resultActions = mvc
                    .perform(
                            post("/api/v1/questions/%d/answers".formatted(targetQuestionId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                        {
                                            "content": "%s",
                                            "isPublic": true
                                        }
                                        """.formatted(answer))
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("createAnswer"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("작성자 정보 저장 확인")
        void success2() throws Exception {
            long targetQuestionId = questionId;
            String answer = "새로운 답변 내용입니다.";

            ResultActions resultActions = mvc
                    .perform(
                            post("/api/v1/questions/%d/answers".formatted(targetQuestionId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                        {
                                            "content": "%s",
                                            "isPublic": true
                                        }
                                        """.formatted(answer))
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("createAnswer"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("CREATED"));

            String responseBody = resultActions.andReturn().getResponse().getContentAsString();
            Long answerId = JsonPath.parse(responseBody).read("$.data.id", Long.class);

            Answer savedAnswer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new RuntimeException("답변이 저장되지 않았습니다."));

            // 작성자 검증
            assertThat(savedAnswer.getAuthor().getId()).isEqualTo(mockUser.getId());
        }

    }

    @Nested
    @DisplayName("답변 수정 테스트")
    class t2 {

        @Test
        @DisplayName("1번 답변 수정 성공")
        void success() throws Exception {
            long targetQuestionId = questionId; // 동적으로 생성된 질문 ID 사용
            long targetAnswerId = answerIdList.get(0); // 동적으로 생성된 답변 ID 사용
            String content = "수정된 답변 내용입니다."; // 수정할 내용
            long expectedAuthorId = mockUser.getId(); // 예상 작성자 ID
            String expectedAuthorNickname = mockUser.getNickname(); // 예상 작성자 닉네임

            // 초기 modifyDate 값 캡처 (수정되기 전의 시간)
            LocalDateTime initialModifyDate = answerRepository.findById(targetAnswerId)
                    .orElseThrow(() -> new AssertionError("답변을 찾을 수 없습니다."))
                    .getModifyDate();

            // modifiedDate가 초기값보다 확실히 이후가 되도록 보장
            Thread.sleep(100);

            ResultActions resultActions = mvc
                    .perform(
                            patch("/api/v1/questions/%d/answers/%d".formatted(targetQuestionId, targetAnswerId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                        {
                                            "content": "%s",
                                            "isPublic": false
                                        }
                                        """.formatted(content))
                    )
                    .andDo(print());

            // [추가] 응답 본문을 통해 답변 ID, 질문 ID, 수정된 내용이 올바른지 검증
            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("updateAnswer")) // PATCH에 맞는 메서드 이름
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("%d번 답변이 수정되었습니다.".formatted(targetAnswerId)))
                    .andExpect(jsonPath("$.data.id").value(targetAnswerId))
                    .andExpect(jsonPath("$.data.createDate").exists())
                    .andExpect(jsonPath("$.data.modifyDate").exists())
                    .andExpect(jsonPath("$.data.content").value(content))
//                    .andExpect(jsonPath("$.data.aiScore").value(Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.isPublic").value(false))
//                    .andExpect(jsonPath("$.data.feedback").value(Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.authorId").value(expectedAuthorId))
                    .andExpect(jsonPath("$.data.authorNickName").value(expectedAuthorNickname))
                    .andExpect(jsonPath("$.data.questionId").value(targetQuestionId));

            answerRepository.flush();

            // DB 확인: 실제로 수정된 내용이 반영되었는지 최종 검증
            Optional<Answer> optionalAnswer = answerRepository.findById(targetAnswerId);
            assertThat(optionalAnswer).isPresent();
            Answer updatedAnswer = optionalAnswer.get();
            assertThat(updatedAnswer.getContent()).isEqualTo(content); // DB에서 변경내용 확인
            assertThat(updatedAnswer.getQuestion().getId()).isEqualTo(targetQuestionId); // DB에서 Question ID 확인
            assertThat(updatedAnswer.getAuthor().getId()).isEqualTo(expectedAuthorId); // DB에서 Author ID 확인
            assertThat(updatedAnswer.getModifyDate()).isAfter(updatedAnswer.getCreateDate()); // 수정일이 생성일 이후인지 확인
            assertThat(updatedAnswer.getModifyDate()).isAfter(initialModifyDate); // 수정 날짜가 초기 날짜보다 이후인지 확인
        }

        @Test
        @DisplayName("답변 수정 - 다른 작성자의 답변 수정 시도")
        void fail1() throws Exception {
            long targetQuestionId = questionId;
            long targetAnswerId = answerIdList.get(2);
            String content = "수정된 답변 내용입니다.";

            ResultActions resultActions = mvc
                    .perform(
                            patch("/api/v1/questions/%d/answers/%d".formatted(targetQuestionId, targetAnswerId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                        {
                                            "content": "%s",
                                            "isPublic": false
                                        }
                                        """.formatted(content))
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("updateAnswer"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("해당 답변에 대한 권한이 없는 사용자입니다."));
        }

        @Test
        @DisplayName("답변 수정 - 없는 답변에 대한 수정 요청 ")
        void fail2() throws Exception {
            long targetQuestionId = questionId;
            long targetAnswerId = 9999;
            String content = "수정된 답변 내용입니다.";

            ResultActions resultActions = mvc
                    .perform(
                            patch("/api/v1/questions/%d/answers/%d".formatted(targetQuestionId, targetAnswerId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                        {
                                            "content": "%s",
                                            "isPublic": false
                                        }
                                        """.formatted(content))
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("updateAnswer"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("존재하지 않는 답변입니다."));
        }

    }

    @Nested
    @DisplayName("답변 삭제 테스트")
    class t3 {

        @Test
        @DisplayName("답변 삭제 - 1번 질문의 1번 답변 삭제")
        void success() throws Exception {
            long targetQuestionId = questionId;
            long targetAnswerId = answerIdList.get(0);

            ResultActions resultActions = mvc
                    .perform(
                            delete("/api/v1/questions/%d/answers/%d".formatted(targetQuestionId, targetAnswerId))
                    )
                    .andDo(print());

            // 필수 검증
            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("deleteAnswer"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("%d번 답변이 삭제되었습니다.".formatted(targetAnswerId)));

            // 선택적 검증
            Answer answer = answerRepository.findById(targetAnswerId).orElse(null);
            assertThat(answer).isNull();
        }

        @Test
        @DisplayName("답변 삭제 - 다른 사용자의 답변 삭제 시도")
        void fail1() throws Exception {
            long targetQuestionId = questionId;
            long targetAnswerId = answerIdList.get(2);

            ResultActions resultActions = mvc
                    .perform(
                            delete("/api/v1/questions/%d/answers/%d".formatted(targetQuestionId, targetAnswerId))
                    )
                    .andDo(print());

            // 필수 검증
            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("deleteAnswer"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("해당 답변에 대한 권한이 없는 사용자입니다."));

            // 선택적 검증
            Answer answer = answerRepository.findById(targetAnswerId).orElse(null);
            assertThat(answer).isNotNull();
        }

    }

    @Nested
    @DisplayName("답변 목록 조회 테스트")
    class t4 {

        @Test
        @DisplayName("답변 목록 조회")
        void success() throws Exception {

            long targetQuestionId = questionId;

            ResultActions resultActions = mvc
                    .perform(
                            get("/api/v1/questions/%d/answers".formatted(targetQuestionId))
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("readAnswers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("%d번 질문의 답변 목록 조회 성공".formatted(targetQuestionId)));

            resultActions
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$.data.answers[*].id", containsInRelativeOrder(answerIdList.getLast().intValue(), answerIdList.getLast().intValue() - 9)))
                    .andExpect(jsonPath("$.data.answers[0].id").value(answerIdList.getLast()))
                    .andExpect(jsonPath("$.data.answers[0].createDate").exists())
                    .andExpect(jsonPath("$.data.answers[0].modifyDate").exists())
                    .andExpect(jsonPath("$.data.answers[0].content").value("열세 번째 답변 내용"))
//                    .andExpect(jsonPath("$.data.answers[0].score").value(10))
                    .andExpect(jsonPath("$.data.answers[0].isPublic").value(true))
//                    .andExpect(jsonPath("$.data.answers[0].feedback").value("좋은 답변입니다."))
                    .andExpect(jsonPath("$.data.answers[0].authorId").value(userIdList.get(0)))
                    .andExpect(jsonPath("$.data.answers[0].authorNickName").value("gildong"))
                    .andExpect(jsonPath("$.data.answers[0].questionId").value(targetQuestionId))
                    .andExpect(jsonPath("$.data.answers[*].isPublic").value(Matchers.not(Matchers.hasItem(false))))
                    .andExpect(jsonPath("$.data.answers[*].id").value(Matchers.not(Matchers.hasItem(2))));
        }

        @Test
        @DisplayName("답변 목록 조회, 2페이지")
        void success2() throws Exception {

            long targetQuestionId = questionId;

            ResultActions resultActions = mvc
                    .perform(
                            get("/api/v1/questions/%d/answers?page=2".formatted(targetQuestionId))
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("readAnswers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("%d번 질문의 답변 목록 조회 성공".formatted(targetQuestionId)));

            resultActions
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$.data.answers[*].id", containsInRelativeOrder(answerIdList.getFirst().intValue() + 2, answerIdList.getFirst().intValue())))
                    .andExpect(jsonPath("$.data.answers[0].id").value(answerIdList.getFirst().intValue() + 2))
                    .andExpect(jsonPath("$.data.answers[0].createDate").exists())
                    .andExpect(jsonPath("$.data.answers[0].modifyDate").exists())
                    .andExpect(jsonPath("$.data.answers[0].content").value("세 번째 답변 내용"))
//                    .andExpect(jsonPath("$.data.answers[0].score").value(10))
                    .andExpect(jsonPath("$.data.answers[0].isPublic").value(true))
//                    .andExpect(jsonPath("$.data.answers[0].feedback").value("좋은 답변입니다."))
                    .andExpect(jsonPath("$.data.answers[0].authorId").value(userIdList.get(0)))
                    .andExpect(jsonPath("$.data.answers[0].authorNickName").value("gildong"))
                    .andExpect(jsonPath("$.data.answers[0].questionId").value(targetQuestionId))
                    .andExpect(jsonPath("$.data.answers[*].isPublic").value(Matchers.not(Matchers.hasItem(false))))
                    .andExpect(jsonPath("$.data.answers[*].id").value(Matchers.not(Matchers.hasItem(2))));
        }

        @Test
        @DisplayName("답변 목록 조회 - 존재하지 않는 질문")
        void fail1() throws Exception {
            long targetQuestionId = 9999;

            ResultActions resultActions = mvc
                    .perform(
                            get("/api/v1/questions/%d/answers".formatted(targetQuestionId))
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("readAnswers"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("답변 단건 조회 테스트")
    class t5 {

        @Test
        @DisplayName("내 답변 조회")
        void success() throws Exception {

            long targetQuestionId = questionId;

            ResultActions resultActions = mvc
                    .perform(
                            get("/api/v1/questions/%d/answers/mine".formatted(targetQuestionId))
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("readMyAnswer"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("%d번 질문의 내 답변 조회 성공".formatted(targetQuestionId)));

            resultActions
                    .andExpect(jsonPath("$.data.id").value(answerIdList.get(0)))
                    .andExpect(jsonPath("$.data.createDate").exists())
                    .andExpect(jsonPath("$.data.modifyDate").exists())
                    .andExpect(jsonPath("$.data.content").value("첫 번째 답변 내용"))
                    .andExpect(jsonPath("$.data.isPublic").value(true))
//                    .andExpect(jsonPath("$.data.feedback").value(Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.authorId").value(mockUser.getId()))
                    .andExpect(jsonPath("$.data.authorNickName").value(mockUser.getNickname()))
                    .andExpect(jsonPath("$.data.questionId").value(targetQuestionId));
        }

        @Test
        @DisplayName("내 답변 조회 - 존재하지 않는 질문의 답변 조회 시도")
        void fail3() throws Exception {
            long targetQuestionId = 9999;

            ResultActions resultActions = mvc
                    .perform(
                            get("/api/v1/questions/%d/answers/mine".formatted(targetQuestionId))
                    )
                    .andDo(print());

            resultActions
                    .andExpect(handler().handlerType(AnswerController.class))
                    .andExpect(handler().methodName("readMyAnswer"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("질문을 찾을 수 없습니다."));
        }

    }

}