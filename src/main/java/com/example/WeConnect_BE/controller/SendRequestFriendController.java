package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.request.FriendRequest;
import com.example.WeConnect_BE.dto.response.FriendPendingResponse;
import com.example.WeConnect_BE.dto.response.FriendResponse;
import com.example.WeConnect_BE.entity.Friend;
import com.example.WeConnect_BE.repository.FriendRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import com.example.WeConnect_BE.service.SendRequestFriendService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
    @RequestMapping("/friend-request")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SendRequestFriendController {
    SendRequestFriendService sendRequestFriendService;

    @PostMapping("/send")
    public ApiResponse<FriendResponse> sendFriendRequest(@RequestBody FriendRequest friendRequest) {
        return ApiResponse.<FriendResponse>builder()
                .result(sendRequestFriendService.sendFriendRequest(friendRequest))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<List<FriendPendingResponse>> getFriendRequest(@PathVariable("id") String userId) {
        return ApiResponse.<List<FriendPendingResponse>>builder()
                .result(sendRequestFriendService.getFriends(userId))
                .build();
    }

    @PostMapping("/accepted")
    public ApiResponse<FriendResponse> acceptFriendRequest(@RequestBody FriendRequest friendRequest) {
        return ApiResponse.<FriendResponse>builder()
                .result(sendRequestFriendService.acceptFriendRequest(friendRequest.getFrom(), friendRequest.getTo()))
                .build();
    }
    @PostMapping("/rejected")
    public ApiResponse<FriendResponse> rejectFriendRequest(@RequestBody FriendRequest friendRequest) {
        return ApiResponse.<FriendResponse>builder()
                .result(sendRequestFriendService.rejectFriendRequest(friendRequest.getFrom(), friendRequest.getTo()))
                .build();
    }
}
