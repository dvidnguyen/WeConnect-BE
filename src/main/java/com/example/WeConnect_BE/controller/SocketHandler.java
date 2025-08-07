package com.example.WeConnect_BE.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.example.WeConnect_BE.dto.request.IntrospectRequest;
import com.example.WeConnect_BE.dto.response.IntrospectResponse;
import com.example.WeConnect_BE.entity.UserSession;
import com.example.WeConnect_BE.service.AuthenticationService;
import com.example.WeConnect_BE.service.WebSocketSessionService;
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

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketHandler {
    SocketIOServer socketIOServer;
    AuthenticationService authenticationService;
    WebSocketSessionService webSocketSessionService;
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



    @PreDestroy
    public void destroy() {
        socketIOServer.stop();
        log.info("SocketIOServer stopped");
    }
}