package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken, String> {
    boolean existsById(String token);
}