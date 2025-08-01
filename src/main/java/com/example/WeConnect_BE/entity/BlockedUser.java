package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "BLOCKED_USER")
public class BlockedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id")
    private User blockedUser;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @PrePersist
    public void prePersist() {
        if (blockedAt == null) {
            blockedAt = LocalDateTime.now();  // Tự động gán thời gian hiện tại nếu không có
        }
    }
}
