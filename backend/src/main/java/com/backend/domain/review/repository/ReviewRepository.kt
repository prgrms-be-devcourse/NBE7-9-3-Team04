package com.backend.domain.review.repository;

import com.backend.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 사용자의 모든 리뷰를 최신순으로 조회
    List<Review> findByUserIdOrderByIdDesc(Long userId);

}
