package com.example.WeConnect_BE.entity;

import com.example.WeConnect_BE.Util.TypeNotification;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 255)
    private String id;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TypeNotification type;

    @Column(name = "related_id", length = 36)
    private String relatedId;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserNotification> userNotifications;
}