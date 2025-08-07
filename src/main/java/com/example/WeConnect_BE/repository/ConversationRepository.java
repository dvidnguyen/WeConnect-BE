package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {}
