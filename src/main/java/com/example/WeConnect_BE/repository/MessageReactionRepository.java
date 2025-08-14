package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, String> {
    boolean existsByMessage_IdAndUser_UserId(String messageId, String userId);

    void deleteByMessage_IdAndUser_UserId(String messageId, String userId);

    long countByMessage_Id(String messageId);
}
