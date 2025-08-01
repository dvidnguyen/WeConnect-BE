package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.entity.VerifyCode;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerifyCodeRepository extends JpaRepository<VerifyCode, UUID> {
    Optional<VerifyCode> findTopByUserOrderByCreatedAtDesc(User user);

    List<VerifyCode> findByUser(User user);

    Optional<VerifyCode> findTopByUserAndStatusOrderByCreatedAtDesc(User user, int i);
}
