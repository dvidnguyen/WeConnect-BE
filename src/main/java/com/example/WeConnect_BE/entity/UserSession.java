package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "USER_SESSION")
public class UserSession {
    @Id
    private String sessionId;
    private String userId;
    private Date createdAt;
    private Date expiresAt;
}
