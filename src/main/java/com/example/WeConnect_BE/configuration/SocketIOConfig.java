package com.example.WeConnect_BE.configuration;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

@Configuration

public class SocketIOConfig {
    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setPort(8099);
        config.setOrigin("*");

        // Đăng ký module xử lý Java 8 Time (LocalDateTime)
        // Đăng ký module hỗ trợ Java 8 Date/Time
        config.setJsonSupport(new JacksonJsonSupport(new JavaTimeModule()));

        return new SocketIOServer(config);
    }
}
