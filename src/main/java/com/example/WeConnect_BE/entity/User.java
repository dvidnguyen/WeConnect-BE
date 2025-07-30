package com.example.WeConnect_BE.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "USER")
public class User {
    @Id
    private UUID id;

    private String email;
    private String passwordHash;
    private String username;

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

