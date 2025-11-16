package com.backend.domain.resume.repository;

import com.backend.domain.resume.entity.Resume;
import com.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Boolean existsByUserId(Long userId);
    Optional<Resume> findByUser(User user);
}
