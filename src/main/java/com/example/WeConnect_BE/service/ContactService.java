package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.Util.GetIDCurent;
import com.example.WeConnect_BE.dto.response.ContactResponse;
import com.example.WeConnect_BE.entity.BlockedUser;
import com.example.WeConnect_BE.entity.Contact;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.repository.BlockedUserRepository;
import com.example.WeConnect_BE.repository.ContactRepository;
import com.example.WeConnect_BE.repository.ConversationRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContactService {
    ContactRepository contactRepository;
    UserRepository userRepository;
    BlockedUserRepository blockedUserRepository;
    ConversationRepository conversationRepository;

    public List<ContactResponse> getContacts() {
        String id = GetIDCurent.getId(); // sub trong JWT
        List<Contact> contacts = contactRepository
                .findAllByUserId(id);

        // Map sang ContactResponse
        return contacts.stream()
                .map(contact -> {
                    User otherUser;
                    if (contact.getRequesterUser().getUserId().equals(id)) {
                        otherUser = contact.getAddresseeUser();
                    } else {
                        otherUser = contact.getRequesterUser();
                    }
//                    blockedUserRepository.findBlockedCounterparts()
                    // Tìm conversationId nếu có
                    String conversationId = conversationRepository
                            .findDirectConversationIdBetween(id, otherUser.getUserId())
                            .orElse(null);

                    return ContactResponse.builder()
                            .conversationId(conversationId)
                            .id(otherUser.getUserId())                // id của đối phương
                            .name(otherUser.getUsername())
                            .isBlock(blockedUserRepository.existsByUser_UserIdAndBlockedUser_UserId(id, otherUser.getUserId()))// tên đối phương
                            .email(otherUser.getEmail())          // email đối phương
                            .avatarUrl(otherUser.getAvatarUrl())  // avatar đối phương
                            .build();
                }).toList();
    }

    public String blockContact(String id) {
        String owner_id = GetIDCurent.getId(); // sub trong JWT
        if (owner_id.equals(id)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        if (blockedUserRepository.existsByUser_UserIdAndBlockedUser_UserId(owner_id, id)) {
            throw new AppException(ErrorCode.REPEAT_REQUEST);
        }
        User user = userRepository.findById(owner_id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User blocked = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        BlockedUser bu = BlockedUser.builder()
                .user(user)
                .blockedUser(blocked)
                .build();
        blockedUserRepository.save(bu);
        return "Block thành công";
    }
    @Transactional
    public String unblock(String id) {

        String owner_id = GetIDCurent.getId(); // sub trong JWT
        if (owner_id.equals(id)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        if (blockedUserRepository.existsByUser_UserIdAndBlockedUser_UserId(owner_id, id)) {
            long deleted = blockedUserRepository
                    .deleteByUser_UserIdAndBlockedUser_UserId(owner_id, id);
            if (deleted > 0) {
                return "success";
            } else throw new AppException(ErrorCode.NOT_FOUND);
        }

        return "failure";
    }

    @Transactional
    public void unfriend( String targetUserId) {
        String currentUserId = GetIDCurent.getId();
        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        var exists = contactRepository.findBetween(currentUserId, targetUserId).isPresent();
        if (!exists) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        int affected = contactRepository.deleteBetween(currentUserId, targetUserId);
        if (affected == 0) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
