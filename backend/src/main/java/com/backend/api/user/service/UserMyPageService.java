package com.backend.api.user.service;

import com.backend.api.user.dto.response.UserMyPageResponse;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.userQuestion.entity.UserQuestion;
import com.backend.domain.userQuestion.repository.UserQuestionRepository;
import com.backend.global.Rq.Rq;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class UserMyPageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Rq rq;
    private final UserQuestionRepository userQuestionRepository;


    public UserMyPageResponse modifyUser(Long userId, UserMyPageResponse.UserModify modify) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        String encodedPassword;

        if (modify.getPassword() == null || modify.getPassword().trim().isEmpty()) {
            encodedPassword = user.getPassword(); // 기존 비밀번호 유지
        } else {
            encodedPassword = passwordEncoder.encode(modify.getPassword());
        }

        user.updateUser(
                modify.getEmail(),
                encodedPassword,
                modify.getName(),
                modify.getNickname(),
                modify.getAge(),
                modify.getGithub(),
                modify.getImage()
        );
        if (rq.getUser().getId() != userId) {
            throw new ErrorException(ErrorCode.SELF_INFORMATION);
        }

        User saved = userRepository.save(user);
        return UserMyPageResponse.fromEntity(saved);
    }

    public UserMyPageResponse getInformation(Long userId){
        User users = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_USER));

        return UserMyPageResponse.fromEntity(users);
    }


    public List<UserMyPageResponse.SolvedProblem> getSolvedProblems(Long userId) {

        List<UserQuestion> solvedQuestions = userQuestionRepository.findByUser_Id(userId);

        return solvedQuestions.stream()
                .map(q -> UserMyPageResponse.SolvedProblem.builder()
                        .title(q.getQuestion().getTitle())
                        .modifyDate(q.getModifyDate())
                        .build())
                .toList();
    }

    public boolean verifyPassword(Long userId, String rawPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_USER));

        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
