package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.entity.UserSession;
import com.example.WeConnect_BE.repository.UserSessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketSessionService {
    UserSessionRepository userSessionRepository;

    public void create(UserSession userSession) {
        userSessionRepository.save(userSession);
    }
    public void delete(String sessionId) {
        userSessionRepository.deleteById(UUID.fromString(sessionId));
    }
}
