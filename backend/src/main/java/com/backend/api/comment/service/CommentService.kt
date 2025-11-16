package com.backend.api.comment.service;

import com.backend.api.comment.dto.response.CommentMypageResponse;
import com.backend.api.comment.dto.response.CommentPageResponse;
import com.backend.api.comment.dto.response.CommentResponse;
import com.backend.api.post.service.PostService;
import com.backend.api.user.service.UserService;
import com.backend.domain.comment.entity.Comment;
import com.backend.domain.comment.repository.CommentRepository;
import com.backend.domain.post.entity.Post;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;
import com.backend.global.Rq.Rq;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import lombok.RequiredArgsConstructor;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;
    private final Rq rq;

    public Comment findByIdOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ErrorException(ErrorCode.COMMENT_NOT_FOUND));
    }

    @Transactional
    public CommentResponse writeComment(User currentUser, Long postId, String content) {

        if (!currentUser.validateActiveStatus()) {
            throw new ErrorException(ErrorCode.ACCOUNT_SUSPENDED);
        }

        Post post = postService.findPostByIdOrThrow(postId);

        Comment comment = Comment.builder()
                .content(content)
                .author(currentUser)
                .post(post)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponse(savedComment, null);
    }

    @Transactional
    public CommentResponse updateComment(User currentUser, Long commentId, String newContent) {
        Comment comment = this.findByIdOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ErrorException(ErrorCode.COMMENT_INVALID_USER);
        }

        comment.updateContent(newContent);

        return new CommentResponse(comment, null);
    }

    @Transactional
    public void deleteComment(User currentUser, Long commentId) {
        Comment comment = this.findByIdOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ErrorException(ErrorCode.COMMENT_INVALID_USER);
        }

        commentRepository.delete(comment);
    }

    public CommentPageResponse<CommentResponse> getCommentsByPostId(User currentUser, int page, Long postId) {
        postService.findPostByIdOrThrow(postId);

        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, 20, Sort.by("createDate").ascending());

        Page<Comment> commentsPage = commentRepository.findByPostId(postId, pageable);

        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        List<CommentResponse> comments = commentsPage.getContent()
                .stream()
                .map(comment -> {
                    boolean isMine = comment.getAuthor().getId().equals(currentUserId);
                    return new CommentResponse(comment, isMine);
                })
                .toList();

        return new CommentPageResponse<>(commentsPage, comments);
    }

    public CommentPageResponse<CommentMypageResponse> getCommentsByUserId(int page, Long userId) {
        userService.getUser(userId);
        User currentUser = rq.getUser();
        if(!currentUser.getId().equals(userId) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new ErrorException(ErrorCode.COMMENT_INVALID_USER);
        }

        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, 15, Sort.by("createDate").descending());
        Page<Comment> userCommentsPage = commentRepository.findByAuthorId(userId, pageable);

        List<CommentMypageResponse> userComments = userCommentsPage.getContent()
                .stream()
                .map(CommentMypageResponse::new)
                .toList();

        return new CommentPageResponse<>(userCommentsPage, userComments);
    }
}