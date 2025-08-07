package com.example.WeConnect_BE.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification {

    @Id
    @Column(length = 255)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "notification_id", referencedColumnName = "id")
    private Notification notification;

    @Column(name = "is_read")
    private int isRead;
}
