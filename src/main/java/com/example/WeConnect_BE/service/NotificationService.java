package com.example.WeConnect_BE.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.WeConnect_BE.dto.response.NotificationResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    SocketIOServer socketIOServer;

    public void sendNotification(SocketIOClient client, String event, Object data) {
        if (client != null && client.isChannelOpen()) {
            client.sendEvent(event, data);
        }
    }

    public void readNotification(SocketIOClient client, String event, Object data) {}

}
