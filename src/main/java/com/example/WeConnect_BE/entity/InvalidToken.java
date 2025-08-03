package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "INVALID_TOKEN")
public class InvalidToken {
    @Id
    private String token;
    private Date createdAt;
}
