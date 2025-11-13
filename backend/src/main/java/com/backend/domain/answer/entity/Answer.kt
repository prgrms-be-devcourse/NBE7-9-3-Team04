package com.backend.domain.answer.entity;

import com.backend.domain.feedback.entity.Feedback;
import com.backend.domain.question.entity.Question;
import com.backend.domain.user.entity.User;
import com.backend.global.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class Answer extends BaseEntity {

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(nullable = false)
    private Question question;

    @OneToOne(mappedBy = "answer", cascade = CascadeType.REMOVE,  fetch = FetchType.LAZY)
    private Feedback feedback;

    public void update(String content, Boolean isPublic) {
        if(content != null && !content.isBlank()) {
            this.content = content;
        }
        if(isPublic != null) {
            this.isPublic = isPublic;
        }
    }

}
