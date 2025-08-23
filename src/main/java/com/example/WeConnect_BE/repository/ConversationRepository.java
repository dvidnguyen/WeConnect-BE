package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.dto.response.ConversationRow;
import com.example.WeConnect_BE.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    Optional<Conversation> findByDmKey(String dmKey);

    @Query(value = """
        SELECT
          c.id AS conversationId,

          -- Nếu DIRECT: lấy name/avatar của đối phương; ngược lại: dùng của conversation
          CASE WHEN c.type = 'direct' THEN u2.username ELSE c.name END AS name,
          c.type AS type,
          CASE WHEN c.type = 'direct' THEN u2.avatar_url ELSE c.avatar END AS avatar,

          lm.id        AS lastMessageId,
          lm.content   AS lastMessage,
          lm.sent_at   AS lastMessageTime,
          lm.sender_user_id AS lastMessageSenderId,

          (
            SELECT COUNT(*)
            FROM message m
            LEFT JOIN read_receipt rr
              ON rr.message_id = m.id
             AND rr.user_id = :userId
            WHERE m.conversation_id = c.id
              AND rr.id IS NULL            -- chưa có dấu đọc cho user này
              AND m.sender_user_id <> :userId   -- không tính tin của chính mình
          ) AS unreadCount

        FROM conversation c
        -- thành viên là current user
        JOIN member mem
          ON mem.conversation_id = c.id
         AND mem.user_id = :userId

        -- chỉ join "đối phương" khi là DIRECT để tránh nhân bản hàng
        LEFT JOIN member mem2
          ON mem2.conversation_id = c.id
         AND c.type = 'direct'
         AND mem2.user_id <> :userId

        -- bảng users để lấy username/avatar của đối phương
        LEFT JOIN `users` u2
          ON u2.user_id = mem2.user_id

        -- last message
        LEFT JOIN message lm
          ON lm.id = (
               SELECT m2.id
               FROM message m2
               WHERE m2.conversation_id = c.id
               ORDER BY m2.sent_at DESC
               LIMIT 1
          )

        ORDER BY COALESCE(lm.sent_at, c.created_at) DESC
        """, nativeQuery = true)
    List<ConversationRow> findListWithLastMessageAndUnread(@Param("userId") String userId);
    // >>> NEW: 1 row cho đúng user + conversationId
    @Query(value = """
        SELECT
          c.id AS conversationId,
          CASE WHEN c.type = 'direct' THEN u2.username ELSE c.name END AS name,
          c.type AS type,
          CASE WHEN c.type = 'direct' THEN u2.avatar_url ELSE c.avatar END AS avatar,

          lm.id        AS lastMessageId,
          lm.content   AS lastMessage,
          lm.sent_at   AS lastMessageTime,
          lm.sender_user_id AS lastMessageSenderId,

          (
            SELECT COUNT(*)
            FROM message m
            LEFT JOIN read_receipt rr
              ON rr.message_id = m.id
             AND rr.user_id = :userId
            WHERE m.conversation_id = c.id
              AND rr.id IS NULL
              AND m.sender_user_id <> :userId
          ) AS unreadCount

        FROM conversation c
        JOIN member mem
          ON mem.conversation_id = c.id
         AND mem.user_id = :userId

        LEFT JOIN member mem2
          ON mem2.conversation_id = c.id
         AND c.type = 'direct'
         AND mem2.user_id <> :userId

        LEFT JOIN `users` u2
          ON u2.user_id = mem2.user_id

        LEFT JOIN message lm
          ON lm.id = (
               SELECT m2.id
               FROM message m2
               WHERE m2.conversation_id = c.id
               ORDER BY m2.sent_at DESC
               LIMIT 1
          )
        WHERE c.id = :conversationId
        """, nativeQuery = true)
    Optional<ConversationRow> findOneRowForUser(
            @Param("userId") String userId,
            @Param("conversationId") String conversationId);

    @Query("""
    select c.id
    from Conversation c
    where c.type = 'direct'
      and size(c.members) = 2
      and exists (
         select 1 from Member m1
         where m1.conversation = c and m1.user.userId = :userId1
      )
      and exists (
         select 1 from Member m2
         where m2.conversation = c and m2.user.userId = :userId2
      )
""")
    Optional<String> findDirectConversationIdBetween(@Param("userId1") String userId1,
                                                     @Param("userId2") String userId2);

}
