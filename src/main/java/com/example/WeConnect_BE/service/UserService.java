package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.Util.GetIDCurent;
import com.example.WeConnect_BE.Util.PhoneUtil;
import com.example.WeConnect_BE.dto.request.EditUserRequest;
import com.example.WeConnect_BE.dto.response.SearchUserResponse;
import com.example.WeConnect_BE.dto.response.UserProfileResponse;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.mapper.UserMapper;
import com.example.WeConnect_BE.repository.BlockedUserRepository;
import com.example.WeConnect_BE.repository.ContactRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    BlockedUserRepository blockedUserRepository;
    ContactRepository contactRepository;

    public List<SearchUserResponse> search(String q) {
        List<SearchUserResponse> results;
        String currentUserId = GetIDCurent.getId();
        if (q == null || q.isBlank()) {
            return List.of();
        }


        // Nếu là email
        if (PhoneUtil.looksLikeEmail(q)) {
            results = userRepository.findByEmailIgnoreCase(q.trim())
                    .map(userMapper::toSearchUserResponse)     // map sang DTO
                    .map(List::of)                              // list 1 phần tử
                    .orElseGet(List::of);                       // rỗng
        } else if (PhoneUtil.looksLikePhone(q)) {

            results = userRepository.findByPhoneNormalized(q)
                    .map(userMapper::toSearchUserResponse)
                    .map(List::of)
                    .orElseGet(List::of);
        } else {

            // Fallback: tìm gần đúng
            results =  userRepository.searchLoose(q.trim(), q)
                    .stream()
                    .map(userMapper::toSearchUserResponse)
                    .toList();
        }


        results = results.stream()
                .filter(r -> !currentUserId.equals(r.getUserId()))
                .toList();
        if (results.isEmpty()) return results;
        populateFriendAndBlockFlags(currentUserId, results);

        results = results.stream()
                .filter(r -> !r.isBlocked())
                .collect(Collectors.toList());
        return results;
    }


    private void populateFriendAndBlockFlags(String currentUserId, List<SearchUserResponse> list) {
        // Lấy danh sách ids cần check
        List<String> otherIds = list.stream()
                .map(SearchUserResponse::getUserId)
                .distinct()
                .toList();

        if (otherIds.isEmpty()) return;

        // Bạn bè: từ Contact (bất kể chiều)
        var friendIds = new HashSet<>(contactRepository.findContactUserIdsWith(currentUserId, otherIds));

        // Block: từ BlockedUser (bất kể chiều)
        var blockedIds = new HashSet<>(blockedUserRepository.findBlockedCounterparts(currentUserId, otherIds));

        // Gán cờ
        list.forEach(r -> {
            r.setFriend(friendIds.contains(r.getUserId()));
            r.setBlocked(blockedIds.contains(r.getUserId()));
        });
    }


    public void editUser( EditUserRequest req) {

        String userId = GetIDCurent.getId(); // sub trong JWT
        // Kiểm tra trùng số điện thoại
        if (req.getPhone() != null && userRepository.existsByPhoneAndNotUserId(req.getPhone(), userId)) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS); // bạn tự định nghĩa ErrorCode
        }
        // Lấy user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Cập nhật field
        user.setUsername(req.getUsername());
        user.setBirthDate(req.getBirthDate());
        user.setPhone(req.getPhone());
        user.setUpdatedAt(LocalDateTime.now());


        // Lưu
        userRepository.save(user);
    }

    public UserProfileResponse getUserProfile() {
        String userId = GetIDCurent.getId();
        Optional<User> user = Optional.ofNullable(userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        ));

       UserProfileResponse res =  userMapper.toUserProfileResponse(user.get());
       return res;
    }

    public SearchUserResponse getOtherProfile(String id) {

        String currentUser = GetIDCurent.getId();
        String targetUser = id;
        Optional<User> user = Optional.ofNullable(userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        ));

        SearchUserResponse res =  userMapper.toSearchUserResponse(user.get());
        if(blockedUserRepository.existsByUser_UserIdAndBlockedUser_UserId(targetUser, currentUser))
        {
            throw new AppException(ErrorCode.BE_BLOCKED);
        }
        // Kiểm tra block
        boolean isBlocked = blockedUserRepository.existsByUser_UserIdAndBlockedUser_UserId(currentUser, targetUser);
        res.setBlocked(isBlocked);

        // Kiểm tra bạn bè
        boolean isFriend = contactRepository.existsByRequesterUser_UserIdAndAddresseeUser_UserId(currentUser, targetUser)
                || contactRepository.existsByRequesterUser_UserIdAndAddresseeUser_UserId(targetUser, currentUser);
        res.setFriend(isFriend);

        return res;
    }



}
