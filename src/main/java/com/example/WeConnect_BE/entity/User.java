package com.example.WeConnect_BE.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"USER\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String email;

    private String username;
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "avatar_url")
    private String avatarUrl;
    private String status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VerifyCode> verifyCodes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BlockedUser> blockedUsers;

    @OneToMany(mappedBy = "blockedUser", cascade = CascadeType.ALL)
    private List<BlockedUser> blockedByOthers;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
    private List<Friend> sentFriendRequests;

    @OneToMany(mappedBy = "addressee", cascade = CascadeType.ALL)
    private List<Friend> receivedFriendRequests;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<MessageReaction> reactions;
}

