package com.example.WeConnect_BE.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.request.EditUserRequest;
import com.example.WeConnect_BE.dto.response.SearchUserResponse;
import com.example.WeConnect_BE.dto.response.UserProfileResponse;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @GetMapping("/search")
    public ApiResponse<List<SearchUserResponse>> searchUsers(@RequestParam("q") String q) {

        return ApiResponse.<List<SearchUserResponse>>builder()
                .code(200)
                .message("success")
                .result(userService.search(q))
                .build();
    }

    // api edit user profile
    @PostMapping("/edit")
    public ApiResponse<String> editUser(@RequestBody EditUserRequest req) {
        userService.editUser(req);
        return ApiResponse.<String>builder()
                .result("User updated successfully")
                .build();
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile() {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userService.getUserProfile())
                .build();
    }

    @GetMapping("/profile/{id}")
    public ApiResponse<SearchUserResponse> getOtherProfile(@PathVariable("id") String id) {
        return ApiResponse.<SearchUserResponse>builder()
                .result(userService.getOtherProfile(id))
                .build();
    }
}
