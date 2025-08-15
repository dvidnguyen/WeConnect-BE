    package com.example.WeConnect_BE.entity;

    import jakarta.persistence.Entity;
    import jakarta.persistence.*;
    import lombok.Data;
    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.UUID;

    @Entity
    @Data
    @Table(name = "message_reaction")
    public class MessageReaction {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private String id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "message_id")
        private Message message;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        private User user;

        private String emoji;

        @Column(name = "reacted_at")
        private LocalDateTime reactedAt;

        @PrePersist
        public void prePersist() {
            if (reactedAt == null) {
                reactedAt = LocalDateTime.now();
            }
        }
    }