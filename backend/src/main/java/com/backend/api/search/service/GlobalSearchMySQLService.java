package com.backend.api.search.service;

import com.backend.api.search.dto.SearchPageResponse;
import com.backend.api.search.dto.SearchResultDto;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalSearchMySQLService {

    private final UserRepository userRepository;

    public SearchPageResponse<SearchResultDto> searchAll(String keyword, int page, int size) {
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        // 예시: 이름이나 닉네임 LIKE 검색
        Page<User> userPage = userRepository.searchByNameOrNickname("%" + keyword + "%", "%" + keyword + "%", pageable);

        List<SearchResultDto> content = userPage.getContent().stream()
                .map(u -> SearchResultDto.builder()
                        .type("user")
                        .id(String.valueOf(u.getId()))
                        .title(u.getName())
                        .snippet(u.getNickname())
                        .build())
                .toList();

        return SearchPageResponse.from(userPage, content);
    }
}
