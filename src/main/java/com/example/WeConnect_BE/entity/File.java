package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "FILE")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    @Column(name = "fileName")
    private String name;
    @Column(name = "type")
    private String type;

    @Column(name = "url")
    private String url;
    @Column(name = "path")
    private String path;
    @Column(name = "md5checksum")
    private String md5checksum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}