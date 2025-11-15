package com.backend.api.user.init;

import com.backend.domain.user.entity.User;
import com.backend.domain.user.entity.search.UserDocument;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.repository.search.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataSyncRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserSearchRepository userSearchRepository;

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();

        List<UserDocument> docs = users.stream()
                .map(u -> UserDocument.builder()
                        .id(String.valueOf(u.getId()))
                        .name(u.getName())
                        .nickname(u.getNickname())
                        .email(u.getEmail())
                        .build())
                .toList();

        userSearchRepository.saveAll(docs);
        log.info("Elasticsearch 인덱싱 완료: {}건 저장됨", docs.size());
    }
}
