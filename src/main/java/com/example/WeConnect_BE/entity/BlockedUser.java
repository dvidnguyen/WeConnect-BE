package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blocked_user",uniqueConstraints = @UniqueConstraint(name = "uk_block_pair", columnNames = {"user_id", "blocked_user_id"}))
public class BlockedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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
            blockedAt = LocalDateTime.now();
        }
    }
}
