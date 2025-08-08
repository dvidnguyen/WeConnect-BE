package com.example.WeConnect_BE.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.WeConnect_BE.Util.TypeNotification;
import com.example.WeConnect_BE.dto.request.FriendReactionRequest;
import com.example.WeConnect_BE.dto.request.FriendRequest;
import com.example.WeConnect_BE.dto.response.FriendPendingResponse;
import com.example.WeConnect_BE.dto.response.FriendResponse;
import com.example.WeConnect_BE.dto.response.NotificationResponse;
import com.example.WeConnect_BE.entity.Contact;
import com.example.WeConnect_BE.entity.Friend;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.entity.UserSession;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.mapper.FriendMapper;
import com.example.WeConnect_BE.repository.ContactRepository;
import com.example.WeConnect_BE.repository.FriendRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import com.example.WeConnect_BE.repository.UserSessionRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    FriendMapper  friendMapper;
    SocketIOServer socketIOServer;

    public FriendResponse sendFriendRequest(FriendRequest request) {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = authentication.getToken();
        String from = jwt.getSubject(); // sub trong JWT
        // 1. Kiểm tra đã có yêu cầu chưa
        boolean exists = friendRepository.existsByRequesterUserIdAndAddresseeUserId(from, request.getTo());
        if (exists) return FriendResponse.builder()
                .success(true)
                .build();
        Optional<User> requester = userRepository.findById(from);
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
    @Transactional
    public FriendResponse acceptFriendRequest(FriendReactionRequest request) {
        var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = auth.getToken().getSubject();

        // 1) Lock friend by id
        Friend friend = friendRepository.lockById(request.getId())
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        // 1.1) chỉ addressee được accept
        if (!friend.getAddressee().getUserId().equals(currentUserId)) {
            return  FriendResponse.builder()
                    .success(false)
                    .build();
        }

        // 2) Set ACCEPTED (idempotent)
        if (friend.getStatus() != Friend.FriendStatus.ACCEPTED) {
            friend.setStatus(Friend.FriendStatus.ACCEPTED);
            friend.setUpdatedAt(LocalDateTime.now());
            friendRepository.saveAndFlush(friend); // giữ thứ tự
        }

        // 3) Upsert Contact (chuẩn hoá cặp a<b)
        upsertContactNormalized(friend);

        // 4) Xoá friend request
        friendRepository.delete(friend);

        // (tùy) gửi notify cho requester

        try {
            Optional<User> requester = userRepository.findById(friend.getRequester().getUserId());
            String sesssionId = userSessionRepository.findByUserId(requester.get().getUserId()).getSessionId();
            SocketIOClient client = socketIOServer.getClient(UUID.fromString(sesssionId));
            Optional<User> addressee = userRepository.findById(currentUserId);
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
    private void upsertContactNormalized(Friend friend) {
        String u1 = friend.getRequester().getUserId();
        String u2 = friend.getAddressee().getUserId();

        // chuẩn hoá: a < b (so sánh chuỗi id)
        String a = (u1.compareTo(u2) < 0) ? u1 : u2;
        String b = (u1.compareTo(u2) < 0) ? u2 : u1;

        // Nếu đã có (a,b) -> thôi
        if (contractRepository.existsByRequesterUser_UserIdAndAddresseeUser_UserId(a, b)) {
            return;
        }

        // Chưa có -> tạo mới. KHÔNG tự set id nếu dùng @GeneratedValue.
        Contact c = Contact.builder()
                .requesterUser(User.builder().userId(a).build())     // chỉ cần set id (managed do đang trong txn)
                .addresseeUser(User.builder().userId(b).build())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            contractRepository.saveAndFlush(c);
        } catch (DataIntegrityViolationException e) {
            // Một request khác vừa tạo xong contact (race) -> bỏ qua
        }
    }

    public FriendResponse rejectFriendRequest(FriendReactionRequest request) {
        // 1) Lấy current user từ JWT
        var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = auth.getToken().getSubject();

        // 2) Khóa bản ghi Friend theo id để tránh 2 luồng xử lý cùng lúc
        Friend friend = friendRepository.lockById(request.getId())
                .orElseThrow(() ->  new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        // 3) Chỉ addressee mới được reject
        //    (đổi getId() -> getUserId() nếu entity User của bạn đặt field là userId)
        if (!friend.getAddressee().getUserId().equals(currentUserId)) {
            return FriendResponse.builder().success(false).build();
        }

        // 4) Set trạng thái REJECTED (idempotent)
        if (friend.getStatus() != Friend.FriendStatus.REJECTED) {
            friend.setStatus(Friend.FriendStatus.REJECTED);
            friend.setUpdatedAt(LocalDateTime.now());
            friendRepository.saveAndFlush(friend);
        }

        // 5) Xóa yêu cầu (tuỳ yêu cầu nghiệp vụ – thường reject là xoá)
        friendRepository.delete(friend);

        // 6) Gửi thông báo cho requester (người nhận notify)
        try {
            User requester = friend.getRequester();
            User addressee = friend.getAddressee();

            // lấy session người nhận notify
           UserSession userSession =  userSessionRepository.findByUserId(requester.getUserId());
            String sid = userSession.getSessionId();
            // kiểu UUID
                SocketIOClient client = socketIOServer.getClient(UUID.fromString(sid));// kiểu UUID nếu bạn mapping như vậy
                        notificationService.sendNotification(
                                client,
                                "friend-rejected",
                                NotificationResponse.builder()
                                        .id(friend.getId())
                                        .title(addressee.getUsername())
                                        .body( "Friend request rejected")
                                        .type("FRIEND")
                                        .isRead(false)
                                        .senderId(addressee.getUserId())
                                        .senderName(addressee.getUsername())
                                        .senderAvatarUrl(addressee.getAvatarUrl())
                                        .relatedId(friend.getId())
                                        .createdAt(LocalDateTime.now())
                                        .build(),
                                TypeNotification.friend_request
                        );

        } catch (Exception ignore) { /* log nếu cần */ }

        return FriendResponse.builder().success(true).build();
    }

    public List<FriendPendingResponse> getFriends() {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = authentication.getToken();
        String userId = jwt.getSubject(); // sub trong JWT
        List<Friend> pendingRequests = friendRepository
                .findByAddressee_UserIdAndStatus(userId, Friend.FriendStatus.PENDING);

        return pendingRequests.stream()
                .map(friend -> FriendPendingResponse.builder()
                        .id(friend.getId())
                        .requesterId(friend.getRequester().getUserId())
                        .requesterAvatar(friend.getRequester().getAvatarUrl())
                        .requesterName(friend.getRequester().getUsername())
                        .sentAt(friend.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }


}
