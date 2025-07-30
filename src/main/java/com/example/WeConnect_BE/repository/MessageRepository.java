package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {}
