package com.example.WeConnect_BE.Util;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.WeConnect_BE.entity.UserSession;
import com.example.WeConnect_BE.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketEmitter {
    private final SocketIOServer server;
    private final UserSessionRepository userSessionRepo;

    /** Gửi tới MỘT user (mọi session của user đó) */
    public void emitToUser(String userId, String event, Object payload) {
        List<UserSession> sessions = userSessionRepo.findByUserId(userId);
        for (UserSession s : sessions) {
            SocketIOClient client = safeGetClient(s.getSessionId());
            if (client != null) client.sendEvent(event, payload);
        }
    }

    /** Gửi tới NHIỀU user một lượt */
    public void emitToUsers(Collection<String> userIds, String event, Object payload) {
        if (userIds == null || userIds.isEmpty()) return;
        List<UserSession> sessions = userSessionRepo.findByUserIdIn(userIds);
        for (UserSession s : sessions) {
            SocketIOClient client = safeGetClient(s.getSessionId());
            if (client != null) client.sendEvent(event, payload);
        }
    }


    /** Đảm bảo chỉ emit SAU KHI COMMIT DB */
    public void emitAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { task.run(); }
            });
        } else {
            task.run();
        }
    }

    private SocketIOClient safeGetClient(String sessionId) {
        try {
            UUID sid = UUID.fromString(sessionId);
            SocketIOClient c = server.getClient(sid);
            return (c != null && c.isChannelOpen()) ? c : null;
        } catch (Exception e) {
            log.debug("Invalid sessionId: {}", sessionId);
            return null;
        }
    }
}
