package com.example.WeConnect_BE.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.WeConnect_BE.Util.TypeNotification;
import com.example.WeConnect_BE.dto.request.FriendRequest;
import com.example.WeConnect_BE.dto.response.FriendPendingResponse;
import com.example.WeConnect_BE.dto.response.FriendResponse;
import com.example.WeConnect_BE.dto.response.NotificationResponse;
import com.example.WeConnect_BE.entity.Friend;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.entity.UserSession;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.repository.ContactRepository;
import com.example.WeConnect_BE.repository.FriendRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import com.example.WeConnect_BE.repository.UserSessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SendRequestFriendService {
    UserRepository userRepository;
    UserSessionRepository userSessionRepository;
    FriendRepository friendRepository;
    NotificationService notificationService;
    ContactRepository contractRepository;
    SocketIOServer socketIOServer;

    public FriendResponse sendFriendRequest(FriendRequest request) {
        // 1. Kiểm tra đã có yêu cầu chưa
        boolean exists = friendRepository.existsByRequesterUserIdAndAddresseeUserId(request.getFrom(), request.getTo());
        if (exists) return FriendResponse.builder()
                .success(true)
                .build();
        Optional<User> requester = userRepository.findById(request.getFrom());
        Optional<User> addressee = userRepository.findById(request.getTo());
        // 2. Tạo bản ghi friend (pending)
        Friend friend = Friend.builder()
                .requester(requester.get())
                .addressee(addressee.get())
                .status(Friend.FriendStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        friendRepository.save(friend);
        try {
            UserSession userSession =  userSessionRepository.findByUserId(request.getTo());
            SocketIOClient client = socketIOServer.getClient(UUID.fromString(userSession.getSessionId()));
            notificationService.sendNotification(client,"friend", NotificationResponse
                    .builder()
                    .id(friend.getId())
                    .body(request.getBody())
                    .title(requester.get().getUsername())
                    .type("FRIEND")
                    .isRead(false)
                    .senderId(requester.get().getUserId())
                    .senderName(requester.get().getUsername())
                    .senderAvatarUrl(requester.get().getAvatarUrl())
                    .createdAt(LocalDateTime.now())
                    .build(), TypeNotification.friend_request
            );
        } catch (Exception e) {

        }
        // 3. Tạo thông báous

        return FriendResponse.builder()
                .success(true)
                .build();
    }

    public FriendResponse acceptFriendRequest(String requesterId, String addresseeId) {
        // Tìm bản ghi yêu cầu kết bạn
        Optional<Friend> optionalFriend = friendRepository
                .findByRequesterUserIdAndAddresseeUserId(requesterId, addresseeId);

        if (optionalFriend.isEmpty()) {
            return FriendResponse.builder().success(false).build();
        }

        Friend friend = optionalFriend.get();

        // Cập nhật trạng thái
        friend.setStatus(Friend.FriendStatus.ACCEPTED);
        friend.setUpdatedAt(LocalDateTime.now());
        friendRepository.save(friend);
        // lưu vào bản contact


        // Gửi thông báo cho requester
        Optional<User> requester = userRepository.findById(requesterId);
        try {
            String sesssionId = userSessionRepository.findByUserId(requester.get().getUserId()).getSessionId();
            SocketIOClient client = socketIOServer.getClient(UUID.fromString(sesssionId));
            Optional<User> addressee = userRepository.findById(addresseeId);
            notificationService.sendNotification(client, "friend-accepted", NotificationResponse.builder()
                    .id(friend.getId())
                    .body("Friend request accepted")
                    .title(addressee.get().getUsername())
                    .type("FRIEND")
                    .isRead(false)
                    .senderId(addressee.get().getUserId())
                    .senderName(addressee.get().getUsername())
                    .senderAvatarUrl(addressee.get().getAvatarUrl())
                    .relatedId(friend.getId())
                    .createdAt(LocalDateTime.now())
                    .build(), TypeNotification.friend_request);
        } catch (Exception e) {

        }


        return FriendResponse.builder().success(true).build();
    }

    public FriendResponse rejectFriendRequest(String requesterId, String addresseeId) {
        Optional<Friend> optionalFriend = friendRepository
                .findByRequesterUserIdAndAddresseeUserId(requesterId, addresseeId);

        if (optionalFriend.isEmpty()) {
            return FriendResponse.builder().success(false).build();
        }

        Friend friend = optionalFriend.get();

        // Cập nhật trạng thái
        friend.setStatus(Friend.FriendStatus.REJECTED);
        friend.setUpdatedAt(LocalDateTime.now());
        friendRepository.save(friend);

        // Gửi thông báo cho requester
        try {
            Optional<User> requester = userRepository.findById(requesterId);
            String sesssionId = userSessionRepository.findByUserId(requester.get().getUserId()).getSessionId();
            SocketIOClient client = socketIOServer.getClient(UUID.fromString(sesssionId));
            Optional<User> addressee = userRepository.findById(addresseeId);
            notificationService.sendNotification(client, "friend-rejected", NotificationResponse.builder()
                    .id(friend.getId())
                    .body("Friend request rejected")
                    .title(addressee.get().getUsername())
                    .type("FRIEND")
                    .isRead(false)
                    .senderId(addressee.get().getUserId())
                    .senderName(addressee.get().getUsername())
                    .senderAvatarUrl(addressee.get().getAvatarUrl())
                    .relatedId(friend.getId())
                    .createdAt(LocalDateTime.now())
                    .build(), TypeNotification.friend_request);
        } catch (Exception e) {

        }


        return FriendResponse.builder().success(true).build();
    }

    public List<FriendPendingResponse> getFriends(String userId) {
        List<Friend> pendingRequests = friendRepository
                .findByAddressee_UserIdAndStatus(userId, Friend.FriendStatus.PENDING);

        return pendingRequests.stream()
                .map(friend -> FriendPendingResponse.builder()
                        .requesterId(friend.getRequester().getUserId())
                        .requesterAvatar(friend.getRequester().getAvatarUrl())
                        .requesterName(friend.getRequester().getUsername())
                        .sentAt(friend.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }


}
