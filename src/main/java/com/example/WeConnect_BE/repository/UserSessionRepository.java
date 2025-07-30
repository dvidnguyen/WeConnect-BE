package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {}
