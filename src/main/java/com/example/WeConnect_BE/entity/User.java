package com.example.WeConnect_BE.entity;

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
@Table(name = "\"users\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private String userId;
    @Column(name = "email")
    private String email;
    @Column(name = "username")
    private String username;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "phone")
    private String phone;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "avatar_url")
    private String avatarUrl;
    @Column(name = "status")
    private int status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserNotification> userNotifications;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VerifyCode> verifyCodes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BlockedUser> blockedUsers;

    @OneToMany(mappedBy = "blockedUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BlockedUser> blockedByOthers;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Friend> sentFriendRequests;

    @OneToMany(mappedBy = "addressee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Friend> receivedFriendRequests;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MessageReaction> reactions;
}

