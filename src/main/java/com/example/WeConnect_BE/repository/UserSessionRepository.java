package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    void deleteBySessionId(String sessionId);
}
