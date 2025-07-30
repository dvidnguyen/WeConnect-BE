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
    private UUID id;

    private String name;
    private String type;
    private Long size;
    private String url;

    @ManyToOne
    @JoinColumn(name = "message_id")
    private Message message;

    private LocalDateTime uploadedAt;
}
