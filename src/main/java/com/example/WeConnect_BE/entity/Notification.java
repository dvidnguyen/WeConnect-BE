package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "NOTIFICATION")
public class Notification {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String title;
    private String body;
    private String type;
    @Column(name = "related_id")
    private String relatedId;

    private boolean isRead;

    private LocalDateTime createdAt;
}