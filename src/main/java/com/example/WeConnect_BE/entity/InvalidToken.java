package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "INVALID_TOKEN")
public class InvalidToken {
    @Id
    private UUID token;

    private LocalDateTime createdAt;
}
