package com.backend.domain.comment.repository;

import com.backend.domain.comment.entity.Comment;
import com.backend.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author", "post"})
    Page<Comment> findByPostId(Long postId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "post"})
    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);

}
