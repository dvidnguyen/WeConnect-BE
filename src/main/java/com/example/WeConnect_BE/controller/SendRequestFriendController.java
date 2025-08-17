package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.Util.GetIDCurent;
import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.request.FriendReactionRequest;
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

    @GetMapping
    public ApiResponse<List<FriendPendingResponse>> getFriendRequest() {
        return ApiResponse.<List<FriendPendingResponse>>builder()
                .result(sendRequestFriendService.getFriends())
                .build();
    }

    @PostMapping("/accepted")
    public ApiResponse<FriendResponse> acceptFriendRequest(@RequestBody FriendReactionRequest friendRequest) {
        return ApiResponse.<FriendResponse>builder()
                .result(sendRequestFriendService.acceptFriendRequest(friendRequest))
                .build();
    }
    @PostMapping("/rejected")
    public ApiResponse<FriendResponse> rejectFriendRequest(@RequestBody FriendReactionRequest friendRequest) {
        return ApiResponse.<FriendResponse>builder()
                .result(sendRequestFriendService.rejectFriendRequest(friendRequest))
                .build();
    }

    @DeleteMapping("/cancel/{friendId}")
    public ApiResponse<String> cancelById(@PathVariable String friendId) {
        String me = GetIDCurent.getId();
        sendRequestFriendService.cancelFriendRequestById(me, friendId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("successfull")
                .result("")
                .build();
    }
}
