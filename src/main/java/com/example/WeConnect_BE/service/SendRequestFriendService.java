package com.example.WeConnect_BE.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.WeConnect_BE.Util.TypeNotification;
import com.example.WeConnect_BE.dto.request.FriendRequest;
import com.example.WeConnect_BE.dto.response.FriendPendingResponse;
import com.example.WeConnect_BE.dto.response.FriendResponse;
import com.example.WeConnect_BE.dto.response.NotificationResponse;
import com.example.WeConnect_BE.entity.Contact;
import com.example.WeConnect_BE.entity.Friend;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.entity.UserSession;
import com.example.WeConnect_BE.exception.AppException;
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
    @Transactional
    public FriendResponse acceptFriendRequest(String requesterId, String addresseeId) {
// 1) Lock hàng Friend để tránh 2 request accept cùng lúc
        Friend friend = friendRepository.lockByPair(requesterId, addresseeId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        // Idempotent: nếu đã ACCEPTED, vẫn đảm bảo Contact tồn tại rồi xóa Friend (nếu yêu cầu là xóa)
        if (friend.getStatus() == Friend.FriendStatus.ACCEPTED) {
            upsertContactNormalized(friend);
            friendRepository.delete(friend); // nếu bạn muốn xóa record friend sau khi đã là bạn
            return FriendResponse.builder().success(true).build();
        }

        // 2) Cập nhật trạng thái -> ACCEPTED
        friend.setStatus(Friend.FriendStatus.ACCEPTED);
        friend.setUpdatedAt(LocalDateTime.now());
        friendRepository.saveAndFlush(friend); // flush sớm giữ thứ tự thao tác

        // 3) Tạo Contact nếu chưa có (idempotent + normalized)
        upsertContactNormalized(friend);

        // 4) Xoá friend request
        friendRepository.delete(friend);

        // Gửi thông báo nếu cần
        // ... code gửi thông báo ...
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
