package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.Util.GetIDCurent;
import com.example.WeConnect_BE.Util.SocketEmitter;
import com.example.WeConnect_BE.dto.response.ListNotification;
import com.example.WeConnect_BE.dto.response.NotificationRaw;
import com.example.WeConnect_BE.dto.response.NotificationResponse;
import com.example.WeConnect_BE.entity.UserNotification;
import com.example.WeConnect_BE.repository.NotificationRepository;
import com.example.WeConnect_BE.repository.UserNotificationRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationUserService {
    NotificationRepository notificationRepository;
    UserNotificationRepository userNotificationRepository;

    @Transactional(readOnly = true)
    public ListNotification getNotifications(String userId) {


        List<NotificationRaw> raws = userNotificationRepository.findAllByUser(userId);

        List<NotificationResponse> items = raws.stream().map(r ->
                NotificationResponse.builder()
                        .id(r.getId())
                        .title(r.getTitle())
                        .body(r.getBody())
                        .type(r.getType())
                        .relatedId(r.getRelatedId())
                        .createdAt(r.getCreatedAt() == null ? null :
                                LocalDateTime.ofInstant(r.getCreatedAt().toInstant(), ZoneId.systemDefault()))
                        .isRead(r.isRead())
                        .build()
        ).toList();

        long unread = userNotificationRepository.countByUser_UserIdAndIsRead(userId, 0);

        return ListNotification.builder()
                .notifications(items)
                .unread((int) unread)
                .build();
    }
    @Transactional
    public void markOneRead(String userId, String notificationId) {
        UserNotification un = userNotificationRepository.findByUser_UserIdAndNotification_Id(userId, notificationId);
        if (un != null && un.getIsRead() == 0) {
            un.setIsRead(1);
            userNotificationRepository.save(un);
        }
    }

    @Transactional
    public int markAllRead(String userId) {
        return userNotificationRepository.markAllRead(userId);
    }
}
