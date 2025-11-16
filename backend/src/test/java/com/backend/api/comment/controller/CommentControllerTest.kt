package com.backend.api.comment.controller;


import com.backend.domain.comment.repository.CommentRepository;
import com.backend.domain.post.entity.PinStatus;
import com.backend.domain.post.entity.PostCategoryType;
import com.backend.domain.post.entity.PostStatus;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.comment.entity.Comment;
import com.backend.domain.post.entity.Post;
import com.backend.domain.post.repository.PostRepository;
import com.backend.global.security.CustomUserDetails;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;

//    @BeforeAll
//    @Transactional
//    void setUp() {
//
//        User generalUser = User.builder()
//                .email("general@user.com")
//                .password("asdf1234!")
//                .name("홍길동")
//                .nickname("gildong")
//                .age(20)
//                .github("abc123")
//                .image(null)
//                .role(Role.USER)
//                .build();
//
//        User generalUser2 = User.builder()
//                .email("general2@user.com")
//                .password("asdf1234!")
//                .name("홍길똥")
//                .nickname("gilddong")
//                .age(25)
//                .github("abc1233")
//                .image(null)
//                .role(Role.USER)
//                .build();
//
//        userRepository.save(generalUser);
//        userRepository.save(generalUser2);
//
//        Post post1 = Post.builder()
//                .title("제목")
//                .introduction("소개")
//                .content("내용12321321321321")
//                .deadline(LocalDateTime.now().plusDays(7))
//                .status(PostStatus.ING)
//                .pinStatus(PinStatus.NOT_PINNED)
//                .recruitCount(5)
//                .users(userRepository.findById(1L).orElseThrow())
//                .postCategoryType(PostCategoryType.PROJECT)
//                .build();
//        postRepository.save(post1);
//
//        Comment comment1 = Comment.builder()
//                .content("1번 댓글")
//                .author(userRepository.findById(1L).orElseThrow())
//                .post(post1)
//                .build();
//
//        Comment comment2 = Comment.builder()
//                .content("2번 댓글")
//                .author(userRepository.findById(2L).orElseThrow())
//                .post(post1)
//                .build();
//
//        Comment comment3 = Comment.builder()
//                .content("3번 댓글")
//                .author(userRepository.findById(1L).orElseThrow())
//                .post(post1)
//                .build();
//
//        commentRepository.save(comment1);
//        commentRepository.save(comment2);
//        commentRepository.save(comment3);
//    }

    @BeforeEach
    void setupAuth() {
        User user = userRepository.findById(1L).get();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("댓글 생성 - 1번 게시글에 생성")
    void t1() throws Exception {

        long targetPostId = 1; // 동적으로 생성된 게시글 ID 사용
        String content = "새로운 댓글";

        // DB에 댓글 생성 전 개수
        long initialCommentCount = commentRepository.count();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(targetPostId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        // DB에 댓글 생성 후 개수 확인
        List<Comment> newComments = commentRepository.findAll();
        assertThat(newComments.size()).isEqualTo(initialCommentCount + 1);

        // 생성된 댓글 정보 동적 확인
        Comment createdComment = newComments.stream()
                .filter(c -> c.getContent().equals(content))
                .filter(c -> c.getPost().getId().equals(targetPostId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("새로 생성된 댓글을 찾을 수 없습니다."));

        long createdCommentId = createdComment.getId();

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("createComment"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("%d번 댓글이 생성되었습니다.".formatted(createdCommentId)))
                .andExpect(jsonPath("$.data.id").value(createdCommentId))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.authorId").value(createdComment.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorNickName").value(createdComment.getAuthor().getNickname()))
                .andExpect(jsonPath("$.data.postId").value(targetPostId));
    }

    @Test
    @DisplayName("댓글 생성 실패 - 내용이 비어 있을 때")
    void t1_2() throws Exception {
        long targetPostId = 1;
        String content = "";
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(targetPostId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": null
                                        }
                                        """)
                )
                .andDo(print());
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("댓글 내용을 입력해주세요."))
                .andExpect(jsonPath("$.data").doesNotExist());

    }

    @Test
    @DisplayName("댓글 수정 - 자신의 댓글 수정")
    void t2() throws Exception {
        long targetPostId = 7; // 동적으로 생성된 게시글 ID 사용
        long targetCommentId = 1; // 동적으로 생성된 댓글 ID 사용
        String content = "수정한 댓글"; // 수정할 내용
        long expectedAuthorId = 1; // 예상 작성자 ID
        String expectedAuthorNickname = "user1"; // 예상 작성자 닉네임

        // 초기 modifyDate 값 캡처 (수정되기 전의 시간)
        LocalDateTime initialModifyDate = commentRepository.findById(targetCommentId)
                .orElseThrow(() -> new AssertionError("댓글을 찾을 수 없습니다."))
                .getModifyDate();

        // modifiedDate가 초기값보다 확실히 이후가 되도록 보장
        Thread.sleep(100);

        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/posts/%d/comments/%d".formatted(targetPostId, targetCommentId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        // [추가] 응답 본문을 통해 댓글 ID, 게시글 ID, 수정된 내용이 올바른지 검증
        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("updateComment")) // PATCH에 맞는 메서드 이름
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("%d번 댓글이 수정되었습니다.".formatted(targetCommentId)))
                .andExpect(jsonPath("$.data.id").value(targetCommentId))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.authorId").value(expectedAuthorId))
                .andExpect(jsonPath("$.data.authorNickName").value(expectedAuthorNickname))
                .andExpect(jsonPath("$.data.postId").value(targetPostId));

        commentRepository.flush();

        // DB 확인: 실제로 수정된 내용이 반영되었는지 최종 검증
        Optional<Comment> optionalComment = commentRepository.findById(targetCommentId);
        assertThat(optionalComment).isPresent();
        Comment updatedComment = optionalComment.get();
        assertThat(updatedComment.getContent()).isEqualTo(content); // DB에서 변경내용 확인
        assertThat(updatedComment.getPost().getId()).isEqualTo(targetPostId); // DB에서 Post ID 확인
        assertThat(updatedComment.getAuthor().getId()).isEqualTo(expectedAuthorId); // DB에서 Author ID 확인
        assertThat(updatedComment.getModifyDate()).isAfter(updatedComment.getCreateDate()); // 수정일이 생성일 이후인지 확인
        assertThat(updatedComment.getModifyDate()).isAfter(initialModifyDate); // 수정 날짜가 초기 날짜보다 이후인지 확인
    }

    @Test
    @DisplayName("댓글 수정 - 다른 작성자의 댓글 수정 시도")
    void t3() throws Exception {
        long targetPostId = 14;
        long targetCommentId = 3;
        String content = "수정한 댓글";

        ResultActions resultActions = mvc
                .perform(
                        patch("/api/v1/posts/%d/comments/%d".formatted(targetPostId, targetCommentId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("updateComment"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("해당 댓글에 대한 권한이 없는 사용자입니다."));
    }

    @Test
    @DisplayName("댓글 삭제 - 1번 글의 1번 댓글 삭제")
    void t4() throws Exception {
        long targetPostId = 1;
        long targetCommentId = 1;

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/%d/comments/%d".formatted(targetPostId, targetCommentId))
                )
                .andDo(print());

        // 필수 검증
        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("deleteComment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("%d번 댓글이 삭제되었습니다.".formatted(targetCommentId)));

        // 선택적 검증
        Comment comment = commentRepository.findById(targetCommentId).orElse(null);
        assertThat(comment).isNull();
    }
    
    @Test
    @DisplayName("댓글 삭제 - 다른 사용자의 댓글 삭제 시도")
    void t5() throws Exception {
        long targetPostId = 14;
        long targetCommentId = 3;

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/%d/comments/%d".formatted(targetPostId, targetCommentId))
                )
                .andDo(print());

        // 필수 검증
        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("deleteComment"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("해당 댓글에 대한 권한이 없는 사용자입니다."));
    }

    @Test
    @DisplayName("댓글 조회")
    void t6() throws Exception {

        long targetPostId = 16;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments".formatted(targetPostId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("readComments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("%d번 게시글의 댓글 목록 조회 성공".formatted(targetPostId)));

        resultActions
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.data.comments[*].id", containsInRelativeOrder(5, 6)))
                .andExpect(jsonPath("$.data.comments[1].id").value(6))
                .andExpect(jsonPath("$.data.comments[1].createDate").exists())
                .andExpect(jsonPath("$.data.comments[1].modifyDate").exists())
                .andExpect(jsonPath("$.data.comments[1].content").value("혹시 백엔드 포지션도 모집하시나요?"))
                .andExpect(jsonPath("$.data.comments[1].authorId").value(4))
                .andExpect(jsonPath("$.data.comments[1].authorNickName").value("user4"))
                .andExpect(jsonPath("$.data.comments[1].postId").value(targetPostId));

    }

}
