package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "MESSAGE")
public class Message {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    private String content;

    private LocalDateTime sentAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<MessageReaction> reactions;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<ReadReceipt> readReceipts;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<File> files;
}
