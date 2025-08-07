package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
     void deleteByUserId(String userId) ;
     UserSession findByUserId(String userId) ;
}
