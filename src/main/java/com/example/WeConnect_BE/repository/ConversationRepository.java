package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {}
