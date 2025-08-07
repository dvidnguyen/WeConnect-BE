package com.example.WeConnect_BE.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    SocketIOServer server;

//    @GetMapping
//    public List<User> getAllUsers() {
//        return userService.getAllUsers();
//    }

    @GetMapping("/test")
    public boolean test(@RequestParam("session") String session) {
       SocketIOClient client =  server.getClient(UUID.fromString(session));
       client.sendEvent("message", "test");
       return true;
    }
}
