package com.example.WeConnect_BE.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.WeConnect_BE.Util.TypeNotification;
import com.example.WeConnect_BE.dto.response.NotificationResponse;
import com.example.WeConnect_BE.entity.Notification;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.entity.UserNotification;
import com.example.WeConnect_BE.repository.NotificationRepository;
import com.example.WeConnect_BE.repository.UserNotificationRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    SocketIOServer socketIOServer;
    NotificationRepository notificationRepository;
    UserRepository userRepository;
    UserNotificationRepository userNotificationRepository;
    public void sendNotification(SocketIOClient client, String event, NotificationResponse data, TypeNotification typeNotification) {
        if (client != null && client.isChannelOpen()) {
            client.sendEvent(event, data);
            log.info("đã gửi thông báo");

            Notification notification = Notification.builder()
                    .title(data.getTitle())
                    .body(data.getBody())
                    .type(typeNotification)
                    .relatedId(data.getRelatedId())
                    .createdAt(new Date()) // hoặc sử dụng data.getCreatedAt() nếu có
                    .build();
            notificationRepository.save(notification);

            User user = userRepository.findById(data.getSenderId()).orElse(null);
            if (user != null) {
                UserNotification userNotification = UserNotification.builder()
                        .notification(notification)
                        .user(user)
                        .isRead(data.isRead() ? 1 : 0) // hoặc false mặc định
                        .build();

                userNotificationRepository.save(userNotification);
            } else {
                log.warn("Không tìm thấy người dùng với id = {}", data.getSenderId());
            }
        }  else {
        log.warn("Client null hoặc channel đã đóng.");
    }
    }

    public void readNotification(SocketIOClient client, String event, Object data) {}

}
