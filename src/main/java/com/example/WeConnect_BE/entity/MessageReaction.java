package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "MESSAGE_REACTION")
public class MessageReaction {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "message_id")
    private Message message; // Entity này bạn cần tự định nghĩa tiếp

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String emoji;

    private LocalDateTime reactedAt;
}
