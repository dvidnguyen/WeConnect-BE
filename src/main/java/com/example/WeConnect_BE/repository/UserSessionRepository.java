package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
     void deleteByUserId(String userId) ;
     List<UserSession> findByUserId(String userId) ;

     @Query("select us from UserSession us where us.userId in :userIds")
     List<UserSession> findByUserIdIn(@Param("userIds") Collection<String> userIds);
     void deleteBySessionId(String sessionId);
}
