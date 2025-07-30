package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.VerifyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface VerifyCodeRepository extends JpaRepository<VerifyCode, UUID> {}
