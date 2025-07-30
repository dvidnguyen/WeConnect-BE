package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Entity
@Data
@Table(name = "FRIEND")
public class Friend {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @JoinColumn(name = "addressee_id")
    private User addressee;

    private String status;

    private LocalDateTime createdAt;
}

