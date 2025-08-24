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


    @OnEvent("call:invite")
    public void onCallInvite(SocketIOClient client, com.example.WeConnect_BE.dto.request.CallInvitePayload p) {
        String userId = webSocketSessionService.getUserId(client.getSessionId().toString());
        if (userId == null || p == null || p.getConversationId() == null) return;

        // phải là member
        if (!memberRepository.existsByConversation_IdAndUser_UserId(p.getConversationId(), userId)) return;

        // phát chuông cho các thành viên khác
        List<String> recipients = memberRepository.findUserIdsByConversationExcept(p.getConversationId(), userId);
        socketEmitter.emitToUsers(
                recipients,
                "call:ring",
                com.example.WeConnect_BE.dto.response.CallRingEvent.builder()
                        .conversationId(p.getConversationId())
                        .fromUserId(userId)
                        .media(p.getMedia() == null ? "video" : p.getMedia())
                        .build()
        );
        client.sendEvent("call:invite:ack", "OK");
    }

    @OnEvent("call:accept")
    public void onCallAccept(SocketIOClient client, com.example.WeConnect_BE.dto.request.SimpleConvPayload p) {
        String userId = webSocketSessionService.getUserId(client.getSessionId().toString());
        if (userId == null || p == null || p.getConversationId() == null) return;

        List<String> recipients = memberRepository.findUserIdsByConversationExcept(p.getConversationId(), userId);
        socketEmitter.emitToUsers(
                recipients,
                "call:accepted",
                com.example.WeConnect_BE.dto.response.SimpleConvEvent.builder()
                        .conversationId(p.getConversationId())
                        .userId(userId)
                        .build()
        );
    }

    @OnEvent("call:reject")
    public void onCallReject(SocketIOClient client, com.example.WeConnect_BE.dto.request.SimpleConvPayload p) {
        String userId = webSocketSessionService.getUserId(client.getSessionId().toString());
        if (userId == null || p == null || p.getConversationId() == null) return;

        List<String> recipients = memberRepository.findUserIdsByConversationExcept(p.getConversationId(), userId);
        socketEmitter.emitToUsers(
                recipients,
                "call:rejected",
                com.example.WeConnect_BE.dto.response.SimpleConvEvent.builder()
                        .conversationId(p.getConversationId())
                        .userId(userId)
                        .build()
        );
    }

    @OnEvent("call:end")
    public void onCallEnd(SocketIOClient client, com.example.WeConnect_BE.dto.request.SimpleConvPayload p) {
        String userId = webSocketSessionService.getUserId(client.getSessionId().toString());
        if (userId == null || p == null || p.getConversationId() == null) return;

        List<String> participants = memberRepository.findUserIdsByConversation(p.getConversationId());
        socketEmitter.emitToUsers(
                participants,
                "call:ended",
                com.example.WeConnect_BE.dto.response.SimpleConvEvent.builder()
                        .conversationId(p.getConversationId())
                        .userId(userId)
                        .build()
        );
    }

    @OnEvent("webrtc:offer")
    public void onWebrtcOffer(SocketIOClient client, com.example.WeConnect_BE.dto.request.SdpPayload p) {
        forwardRTC(client, "webrtc:offer", p, null);
    }

    @OnEvent("webrtc:answer")
    public void onWebrtcAnswer(SocketIOClient client, com.example.WeConnect_BE.dto.request.SdpPayload p) {
        forwardRTC(client, "webrtc:answer", p, null);
    }

    @OnEvent("webrtc:candidate")
    public void onWebrtcCandidate(SocketIOClient client, com.example.WeConnect_BE.dto.request.IcePayload p) {
        forwardRTC(client, "webrtc:candidate", null, p);
    }

    // helper chung
    private void forwardRTC(SocketIOClient client, String event,
                            com.example.WeConnect_BE.dto.request.SdpPayload sdp,
                            com.example.WeConnect_BE.dto.request.IcePayload ice) {
        String fromUserId = webSocketSessionService.getUserId(client.getSessionId().toString());
        String conversationId = (sdp != null) ? sdp.getConversationId() : (ice != null ? ice.getConversationId() : null);
        String toUserId = (sdp != null) ? sdp.getToUserId() : (ice != null ? ice.getToUserId() : null);

        if (fromUserId == null || conversationId == null || toUserId == null) return;
        if (!memberRepository.existsByConversation_IdAndUser_UserId(conversationId, fromUserId)) return;

        if (sdp != null) sdp.setFromUserId(fromUserId);
        if (ice != null) ice.setFromUserId(fromUserId);

        socketEmitter.emitToUsers(
                List.of(toUserId),
                event,
                (sdp != null) ? sdp : ice
        );
    }




    @PreDestroy
    public void destroy() {
        socketIOServer.stop();
        log.info("SocketIOServer stopped");
    }
}