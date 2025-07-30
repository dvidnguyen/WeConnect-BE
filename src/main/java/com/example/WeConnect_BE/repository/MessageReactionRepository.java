package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, UUID> {}
