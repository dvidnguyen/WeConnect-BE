package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InvalidTokenRepository extends JpaRepository<InvalidToken, String> {}