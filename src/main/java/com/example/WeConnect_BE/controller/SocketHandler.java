package com.example.WeConnect_BE.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.example.WeConnect_BE.Util.SocketEmitter;
import com.example.WeConnect_BE.dto.request.IntrospectRequest;
import com.example.WeConnect_BE.dto.request.NotifMarkReadPayload;
import com.example.WeConnect_BE.dto.request.ReactionPayload;
import com.example.WeConnect_BE.dto.request.ReadPayload;
import com.example.WeConnect_BE.dto.response.IntrospectResponse;
import com.example.WeConnect_BE.dto.response.NotificationResponse;
import com.example.WeConnect_BE.dto.response.ReactionBroadcast;
import com.example.WeConnect_BE.dto.response.ReadBroadcast;
import com.example.WeConnect_BE.entity.UserSession;
import com.example.WeConnect_BE.repository.MemberRepository;
import com.example.WeConnect_BE.service.*;
import com.nimbusds.jose.JOSEException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketHandler {
    SocketIOServer socketIOServer;
    AuthenticationService authenticationService;
    WebSocketSessionService webSocketSessionService;
    ReadReceiptService readReceiptService;
    MessageReactionService messageReactionService;
    MemberRepository  memberRepository;
    NotificationUserService notificationUserService;
    SocketEmitter  socketEmitter;
    @PostConstruct
    public void init() {
        socketIOServer.addListeners(this);
        socketIOServer.start();

        log.info("SocketIOServer started");
    }
    @OnConnect
    public void onConnected(SocketIOClient client) throws ParseException, JOSEException {
        String token = client.getHandshakeData().getSingleUrlParam("token");
        log.info("Client attempting to connect with token: {}", token);

        IntrospectResponse response = authenticationService.introspect(new IntrospectRequest(token));
        if (response.isValid() == false) {
            log.warn("Authentication failed for token: {}", token);
            client.disconnect(); // Ngắt kết nối nếu token không hợp lệ
            return;
        }
        UserSession userSession = UserSession.builder()
                .sessionId(client.getSessionId().toString())
                .userId(response.getUserId())
                .createdAt(Date.from(Instant.now()))
                .build();
        webSocketSessionService.create(userSession);

        client.sendEvent("connected", "connected");
        log.info("User session created: {}", userSession);

    }
    @OnDisconnect
    public void onDisconnected(SocketIOClient client) {

        webSocketSessionService.delete(client.getSessionId().toString());
        // Gửi sự kiện về client báo ngắt kết nối (nếu client vẫn có thể nhận)
        client.sendEvent("disconnect", "Disconnected from Socket.IO server");
        log.info("Client disconnected: {}", client.getSessionId());
    }
    @OnEvent("notif:mark-read")
    public void onNotifMarkRead(SocketIOClient client, NotifMarkReadPayload payload) {
        String userId = webSocketSessionService.getUserId(client.getSessionId().toString());
        if (userId == null || payload == null || payload.getNotificationId() == null) return;

        notificationUserService.markOneRead(userId, payload.getNotificationId());

        // Trả lại unread mới
        int unread = notificationUserService.getNotifications(userId).getUnread();
        client.sendEvent("notif:unread", unread);

        // Ack
        client.sendEvent("notif:mark-read:ack", payload.getNotificationId());
    }

    // -------- Notification: đánh dấu tất cả đã đọc ----------
    @OnEvent("notif:mark-all-read")
    public void onNotifMarkAllRead(SocketIOClient client) {
        String userId = webSocketSessionService.getUserId(client.getSessionId().toString());
        if (userId == null) return;

        notificationUserService.markAllRead(userId);

        int unread = notificationUserService.getNotifications(userId).getUnread();
        client.sendEvent("notif:unread", unread);
        client.sendEvent("notif:mark-all-read:ack", "OK");
    }

    // -------- Read Receipt ----------
    @OnEvent("receipt")
    public void onRead(SocketIOClient client, ReadPayload payload) {
        String userId = webSocketSessionService.getUserId(client.getSessionId().toString());
        if (userId == null) return;

        ReadBroadcast out = readReceiptService.markAsRead(userId, payload.getMessageId());

        // emit cho tất cả trừ người đọc
        List<String> recipients = memberRepository
                .findUserIdsByConversationExcept(out.getConversationId(), userId);
        socketEmitter.emitToUsers(recipients, "receipt-update", out);

        // ack cho client
        client.sendEvent("receipt-ack", out);
    }

    // -------- Reaction (like/unlike) ----------
    @OnEvent("reaction-like")
    public void onReactLike(SocketIOClient client, ReactionPayload payload) {
        String userId = webSocketSessionService.getUserId(client.getSessionId().toString());
        if (userId == null) return;

        ReactionBroadcast out = messageReactionService
                .setLike(userId, payload.getMessageId(), payload.isLike());

        // emit cho tất cả trừ người thao tác
        List<String> recipients = memberRepository
                .findUserIdsByConversationExcept(out.getConversationId(), userId);
        socketEmitter.emitToUsers(recipients, "reaction-update", out);

        client.sendEvent("reaction-ack", out);
    }




    @PreDestroy
    public void destroy() {
        socketIOServer.stop();
        log.info("SocketIOServer stopped");
    }
}