package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.dto.response.ReactionBroadcast;
import com.example.WeConnect_BE.entity.Message;
import com.example.WeConnect_BE.entity.MessageReaction;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.repository.MemberRepository;
import com.example.WeConnect_BE.repository.MessageReactionRepository;
import com.example.WeConnect_BE.repository.MessageRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageReactionService {
    MessageRepository messageRepository;
    MessageReactionRepository messageReactionRepository;
    MemberRepository memberRepository;
    UserRepository userRepository;
    @Transactional
    public ReactionBroadcast setLike(String userId, String messageId, boolean like) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        String conversationId = msg.getConversation().getId();

        if (!memberRepository.existsByConversation_IdAndUser_UserId(conversationId, userId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        boolean exists = messageReactionRepository
                .existsByMessage_IdAndUser_UserId(messageId, userId);

        if (like && !exists) {
            MessageReaction r = new MessageReaction();
            r.setMessage(msg);
            r.setUser(userRepository.getReferenceById(userId));
            messageReactionRepository.save(r);
        } else if (!like && exists) {
            messageReactionRepository.deleteByMessage_IdAndUser_UserId(messageId, userId);
        }

        long likeCount = messageReactionRepository.countByMessage_Id(messageId);

        ReactionBroadcast out = new ReactionBroadcast();
        out.setConversationId(conversationId);
        out.setMessageId(messageId);
        out.setLikeCount(likeCount);
        return out;
    }
}
