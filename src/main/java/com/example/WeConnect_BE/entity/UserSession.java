package com.example.WeConnect_BE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_session")
public class UserSession {
    @Id
    @Column(name = "sessionId")
    private String sessionId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "created_at")
    private Date createdAt;


    public void ifPresent(Object friend) {

    }
}
