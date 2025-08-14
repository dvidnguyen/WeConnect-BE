package com.example.WeConnect_BE.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.example.WeConnect_BE.Util.CursorUtil;
import com.example.WeConnect_BE.Util.GetIDCurent;
import com.example.WeConnect_BE.Util.SocketEmitter;
import com.example.WeConnect_BE.dto.PageInfo;
import com.example.WeConnect_BE.dto.request.ConversationType;
import com.example.WeConnect_BE.dto.request.CreateConversationRequest;
import com.example.WeConnect_BE.dto.request.InviteMembersRequest;
import com.example.WeConnect_BE.dto.response.*;
import com.example.WeConnect_BE.entity.Conversation;
import com.example.WeConnect_BE.entity.Member;
import com.example.WeConnect_BE.entity.Message;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.repository.ConversationRepository;
import com.example.WeConnect_BE.repository.MemberRepository;
import com.example.WeConnect_BE.repository.MessageRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {
    ConversationRepository conversationRepository;
    UserRepository userRepository;
    MessageRepository messageRepository;
    MemberRepository memberRepository;
    SocketIOServer  server;
    SocketEmitter emitter;

    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest req) {
        String type = req.getType();
        if (type == null) throw new IllegalArgumentException("type is required");
        String CreatorId = GetIDCurent.getId();
        switch (type.toLowerCase()) {
            case "direct":
                return createOrGetDirect(CreatorId, req.getTargetUserId());
            case "group":
                return createGroup(req);
            default:
                throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }

    // ===== DIRECT =====
    @Transactional
    protected ConversationResponse  createOrGetDirect(String userIdA, String userIdB) {
        if (userIdA == null || userIdB == null)
            throw new IllegalArgumentException("creatorId and targetUserId are required for DIRECT");
        if (userIdA.equals(userIdB))
            throw new IllegalArgumentException("Cannot create DIRECT with yourself");

        String u1 = userIdA, u2 = userIdB;
        if (u1.compareTo(u2) > 0) { String t = u1; u1 = u2; u2 = t; }
        String dmKey = "dm:" + u1 + ":" + u2;

        var existed = conversationRepository.findByDmKey(dmKey);
        if (existed.isPresent()) {
            return ConversationResponse.builder()
                    .conversationId(existed.get().getId())
                    .created(false)
                    .build();
        }

        User user1 = userRepository.findById(u1)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User user2 = userRepository.findById(u2)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Conversation conv = new Conversation();
        // nếu bạn dùng enum:
        // conv.setType(ConversationType.DIRECT);
        conv.setType(ConversationType.direct);
        conv.setDmKey(dmKey);
        conv.setCreatedAt(LocalDateTime.now());

        Member m1 = new Member();
        m1.setUser(user1);
        m1.setConversation(conv);
        m1.setRole(Member.Role.member);
        m1.setJoinedAt(LocalDateTime.now());

        Member m2 = new Member();
        m2.setUser(user2);
        m2.setConversation(conv);
        m2.setRole(Member.Role.member);
        m2.setJoinedAt(LocalDateTime.now());

        conv.setMembers(List.of(m1, m2));

        try {
            Conversation saved = conversationRepository.save(conv);
            return ConversationResponse.builder()
                    .conversationId(saved.getId())
                    .created(true)
                    .build();
        } catch (DataIntegrityViolationException e) {
            // race: dm_key đã được tạo song song
            Conversation existing = conversationRepository.findByDmKey(dmKey)
                    .orElseThrow(() -> e);
            return ConversationResponse.builder()
                    .conversationId(existing.getId())
                    .created(false)
                    .build();
        }
    }

    // ===== GROUP =====
    @Transactional
    protected ConversationResponse createGroup(CreateConversationRequest req) {
        String creatorId = GetIDCurent.getId();
        if (creatorId == null) throw new AppException(ErrorCode.BAD_REQUEST);
        if (req.getName() == null || req.getName().isBlank()) throw new AppException(ErrorCode.BAD_REQUEST);

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1) Chuẩn hoá danh sách member: gồm creator + memberIds (loại trùng/null)
        Set<String> memberIdSet = new LinkedHashSet<>();
        memberIdSet.add(creatorId);
        if (req.getMemberIds() != null) {
            memberIdSet.addAll(req.getMemberIds().stream()
                    .filter(Objects::nonNull)
                    .toList());
        }

        // 2) Load users
        List<User> users = memberIdSet.stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)))
                .toList();

        // 3) Tạo conversation + members
        Conversation conv = new Conversation();
        conv.setType(ConversationType.group); // nhóm
        conv.setName(req.getName());
        conv.setCreatedAt(LocalDateTime.now());
        conv.setDmKey(null); // group không dùng dmKey

        List<Member> members = new ArrayList<>();
        for (User u : users) {
            Member m = new Member();
            m.setUser(u);
            m.setConversation(conv);
            m.setJoinedAt(LocalDateTime.now());
            m.setRole(u.getUserId().equals(creatorId) ? Member.Role.admin : Member.Role.member);
            members.add(m);
        }
        conv.setMembers(members);

        Conversation saved = conversationRepository.save(conv);
        Message msg = new Message();
        msg.setConversation(saved);
        msg.setSender(creator);
        msg.setType(Message.Type.invite); // hoặc Type.TEXT/INFO tuỳ enum bạn có
        msg.setContent(creator.getUsername() + " đã tạo nhóm");
        msg.setSentAt(LocalDateTime.now());
        msg = messageRepository.save(msg);
        // 4) SOCKET: đẩy về cho từng user một ConversationRow của chính họ (SAU COMMIT)
        List<String> allMemberIds = users.stream().map(User::getUserId).toList();
        emitter.emitAfterCommit(() -> {
            // (tuỳ chọn) Cho các session hiện online join room = conversationId để nhận message realtime
            // Nếu bạn có hàm này trong SocketEmitter:
            // emitter.joinUsersToRoom(allMemberIds, saved.getId());

            // Gửi 1 "conversation:new" cho từng user được thêm vào group
            for (String uid : allMemberIds) {
                conversationRepository.findOneRowForUser(uid, saved.getId())
                        .ifPresent(row -> emitter.emitToUser(uid, "invite", row));
            }

            // (tuỳ chọn) Tạo & bắn một system message "X đã tạo nhóm" nếu bạn muốn hiển thị dòng hệ thống:

            // emitter.emitToUsers(allMemberIds, "message:new", dto);
        });

        return ConversationResponse.builder()
                .conversationId(saved.getId())
                .created(true)
                .build();
    }


    public List<ConversationRow> getList() {
        String userId = GetIDCurent.getId();
        return conversationRepository.findListWithLastMessageAndUnread(userId);
    }

    public PageResult<MessageResponse> getMessagesPerConversation(String conversationId, Integer limit, String before, String after) {
        String userId = GetIDCurent.getId();

        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        boolean isMember = conv.getMembers().stream()
                .anyMatch(m -> m.getUser().getUserId().equals(userId));

        if (!isMember) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        int size = Math.min(Objects.requireNonNullElse(limit, 20), 100);
        Pageable page = PageRequest.of(0, size);

        List<Message> list;
        if (before != null && !before.isBlank()) {
            var p = CursorUtil.decode(before);
            LocalDateTime cSentAt = LocalDateTime.ofInstant(p.getKey(), ZoneOffset.UTC);
            list = messageRepository.before(conversationId, cSentAt, p.getValue(), page);    // DESC
        } else if (after != null && !after.isBlank()) {
            var p = CursorUtil.decode(after);
            LocalDateTime cSentAt = LocalDateTime.ofInstant(p.getKey(), ZoneOffset.UTC);
            list = messageRepository.afterAsc(conversationId, cSentAt, p.getValue(), page); // ASC
            Collections.reverse(list);                                         // trả DESC
        } else {
            list = messageRepository.firstPage(conversationId, page);                        // DESC
        }

        List<MessageResponse> items = list.stream().map(this::toDTO).toList();

        String startCursor = items.isEmpty() ? null :
                CursorUtil.encode(LocalDateTime.ofInstant(items.get(0).getSentAt(), ZoneOffset.UTC),
                        (items.get(0).getId()));
        String endCursor = items.isEmpty() ? null :
                CursorUtil.encode(LocalDateTime.ofInstant(items.get(items.size()-1).getSentAt(), ZoneOffset.UTC),
                        (items.get(items.size()-1).getId()));

        boolean hasNext=false, hasPrev=false;
        if (!list.isEmpty()) {
            var last = list.get(list.size()-1);
            hasNext = !messageRepository.before(conversationId, last.getSentAt(), last.getId(),
                    PageRequest.of(0,1)).isEmpty();

            var first = list.get(0);
            hasPrev = !messageRepository.afterAsc(conversationId, first.getSentAt(), first.getId(),
                    PageRequest.of(0,1)).isEmpty();
        }

        return PageResult.<MessageResponse>builder()
                .items(items)
                .pageInfo(PageInfo.builder()
                        .hasPrevPage(hasPrev)
                        .hasNextPage(hasNext)
                        .startCursor(startCursor)
                        .endCursor(endCursor)
                        .build())
                .build();
    }

    public MessageResponse toDTO(Message m) {
        return MessageResponse.builder()
                .id(m.getId().toString())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSender().getUserId()) // nếu field khác, đổi cho khớp
                .content(m.getContent())
                .reaction(m.getReactions().size())
                .receipt(m.getReadReceipts().size())
                .mine(GetIDCurent.getId().equals(m.getSender().getUserId()))
                .senderName(m.getSender().getUsername())
                .SenderAvatar(m.getSender().getAvatarUrl())
                .url((List<String>) m.getFiles().stream()
                        .map((file -> file.getUrl())))
                .urlDownload((List<String>) m.getFiles().stream()
                        .map((file -> file.getUrl() + "/download")))
                .type(m.getType() == null ? null : m.getType().name())
                .sentAt(m.getSentAt().toInstant(ZoneOffset.UTC))
                .build();
    }


    @Transactional
    public InviteMembersResponse inviteMembers(String conversationId, String inviterUserId, InviteMembersRequest req) {
        // 0) validate input
        if (req.getUserIds() == null || req.getUserIds().isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // 1) load conversation
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // 2) chỉ cho phép group (không phải direct/DM)
        if (conv.getType() != ConversationType.group) { // đổi enum cho đúng project của bạn
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // 3) kiểm tra inviter là member & admin
        boolean inviterIsMember = conv.getMembers().stream()
                .anyMatch(m -> m.getUser().getUserId().equals(inviterUserId));
        if (!inviterIsMember) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }


        // 4) chuẩn hóa danh sách mời (loại trùng, loại chính inviter)
        Set<String> targetIds = new LinkedHashSet<>(req.getUserIds());
        targetIds.remove(inviterUserId); // không tự mời mình

        if (targetIds.isEmpty()) {
            return InviteMembersResponse.builder()
                    .conversationId(conversationId)
                    .added(List.of())
                    .alreadyMembers(List.of())
                    .notFound(List.of())
                    .build();
        }

        // 5) lấy danh sách user đã trong group
        Set<String> existed = new HashSet<>(memberRepository.findUserIdsByConversation(conversationId));

        // 6) tách userIds: đã là member / cần thêm
        List<String> alreadyMembers = targetIds.stream().filter(existed::contains).toList();
        List<String> toAddIds = targetIds.stream().filter(id -> !existed.contains(id)).toList();

        if (toAddIds.isEmpty()) {
            return InviteMembersResponse.builder()
                    .conversationId(conversationId)
                    .added(List.of())
                    .alreadyMembers(alreadyMembers)
                    .notFound(List.of())
                    .build();
        }

        // 7) fetch Users tồn tại
        List<User> users = userRepository.findAllById(toAddIds);
        Set<String> foundIds = users.stream().map(User::getUserId).collect(Collectors.toSet());
        List<String> notFound = toAddIds.stream().filter(id -> !foundIds.contains(id)).toList();

        // 8) tạo Members cho các user tìm thấy
        Member.Role role = "admin".equalsIgnoreCase(req.getDefaultRole()) ? Member.Role.admin : Member.Role.member;

        List<Member> newMembers = new ArrayList<>();
        for (User u : users) {
            Member m = new Member();
            m.setConversation(conv);
            m.setUser(u);
            m.setRole(role);
            m.setJoinedAt(LocalDateTime.now());
            newMembers.add(m);
        }
        if (!newMembers.isEmpty()) {
            memberRepository.saveAll(newMembers);
        }
        if (!users.isEmpty()) {
            User inviter = userRepository.findById(inviterUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

            String invitedNames = users.stream()
                    .map(User::getUsername) // đổi cho đúng field của bạn
                    .collect(Collectors.joining(", "));

            // Tạo system message: INVITE
            Message msg = new Message();
            msg.setConversation(conv);
            msg.setSender(inviter);
            msg.setType(Message.Type.invite); // enum bạn đã thêm
            msg.setContent(inviter.getUsername() + " đã mời " + invitedNames + " vào nhóm");
            msg.setSentAt(LocalDateTime.now());
            msg = messageRepository.save(msg);

            // Map DTO để bắn socket
            MessageResponse dto = MessageResponse.builder()
                    .id(msg.getId().toString())
                    .conversationId(conv.getId())
                    .senderId(inviter.getUserId())
                    .type(msg.getType().name())
                    .content(msg.getContent())
                    .sentAt(msg.getSentAt().toInstant(ZoneOffset.UTC))
                    .build();

            // Tập người nhận message: thành viên cũ + người mới
            List<String> added = users.stream().map(User::getUserId).toList();


            // ==== EMIT AFTER COMMIT ====
            emitter.emitAfterCommit(() -> {
                // 1) Bắn system message cho tất cả thành viên hiện tại
                emitter.emitToUsers(existed, "message:new", dto);

                // 2) Gửi 1 row conversation mới cho từng user vừa được mời
                for (String uid : added) {
                    conversationRepository.findOneRowForUser(uid, conv.getId())
                            .ifPresent(row -> emitter.emitToUser(uid, "conversation:new", row));
                }

                // (tuỳ chọn) Nếu bạn có "room = conversationId" thì có thể dùng:
                // emitter.emitToRoom(conv.getId(), "message:new", dto);
                // và chỉ cần emit "conversation:new" riêng cho người mới.
            });

            return InviteMembersResponse.builder()
                    .conversationId(conversationId)
                    .added(added)
                    .alreadyMembers(alreadyMembers)
                    .notFound(notFound)
                    .build();
        }

// Nếu đến đây tức là users rỗng (không ai tìm thấy hợp lệ)
        return InviteMembersResponse.builder()
                .conversationId(conversationId)
                .added(List.of())
                .alreadyMembers(alreadyMembers)
                .notFound(notFound)
                .build();
    }

    @Transactional
    public LeaveGroupResponse leaveGroup(String conversationId, String leaverId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (conv.getType() != ConversationType.group) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // 1) Kiểm tra thành viên
        Member me = memberRepository.findByConversation_IdAndUser_UserId(conversationId, leaverId)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

        // 2) Nếu chỉ còn 1 người (mình), rời nhóm -> xóa conversation
        List<String> allUserIds = memberRepository.findUserIdsByConversation(conversationId);
        if (allUserIds.size() == 1 && allUserIds.get(0).equals(leaverId)) {
            // xóa member của mình
            memberRepository.deleteByConversation_IdAndUser_UserId(conversationId, leaverId);
            // (tuỳ chiến lược) xóa cả conversation (cascade messages/members)
            conversationRepository.delete(conv);

            // emit sau commit
            emitter.emitAfterCommit(() -> {
                // cho người rời: xoá hội thoại khỏi danh sách
                emitter.emitToUser(leaverId, "conversation:removed",
                        Map.of("conversationId", conversationId));
            });

            return LeaveGroupResponse.builder()
                    .conversationId(conversationId)
                    .leaverId(leaverId)
                    .conversationDeleted(true)
                    .build();
        }

        // 3) Nếu mình là admin DUY NHẤT, phải chuyển quyền cho người khác trước khi rời
        String promotedUserId = null;
        boolean iAmAdmin = (me.getRole() == Member.Role.admin);
        if (iAmAdmin) {
            long adminCount = memberRepository.countByConversation_IdAndRole(conversationId, Member.Role.admin);
            if (adminCount == 1) {
                // chọn 1 ứng viên (người vào sớm nhất khác mình)
                List<Member> cand = memberRepository.findAdminCandidate(conversationId, leaverId, PageRequest.of(0, 1));
                if (cand.isEmpty()) {
                    // Về lý thuyết không xảy ra vì bước 2 đã xử lý nhóm 1 người
                    throw new AppException(ErrorCode.NOT_FOUND_CANDIDATE);
                }
                Member promote = cand.get(0);
                promote.setRole(Member.Role.admin);
                memberRepository.save(promote);
                promotedUserId = promote.getUser().getUserId();
            }
        }

        // 4) Xóa membership của mình
        memberRepository.deleteByConversation_IdAndUser_UserId(conversationId, leaverId);

        // 5) Tạo system message "LEAVE"
        User leaver = userRepository.findById(leaverId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        String text = leaver.getUsername() + " đã rời nhóm";
        if (promotedUserId != null) {
            User promoted = userRepository.findById(promotedUserId).orElse(null);
            String promotedName = promoted != null ? promoted.getUsername() : promotedUserId;
            text += " (quyền admin chuyển cho " + promotedName + ")";
        }

        Message sys = new Message();
        sys.setConversation(conv);
        sys.setSender(leaver);               // hoặc set 1 "system user" nếu bạn có
        sys.setType(Message.Type.leave);     // enum bạn đã thêm
        sys.setContent(text);
        sys.setSentAt(LocalDateTime.now());
        sys = messageRepository.save(sys);

        // map DTO để bắn socket
        MessageResponse dto = MessageResponse.builder()
                .id(sys.getId().toString())
                .conversationId(conv.getId())
                .senderId(leaver.getUserId())
                .type(sys.getType().name())
                .content(sys.getContent())
                .sentAt(sys.getSentAt().toInstant(ZoneOffset.UTC))
                .build();

        // 6) Emit sau commit
        final String promotedFinal = promotedUserId; // capture effectively final
        emitter.emitAfterCommit(() -> {
            // a) Thông báo message system cho những NGƯỜI CÒN LẠI trong nhóm
            List<String> remain = memberRepository.findUserIdsByConversation(conversationId);
            if (!remain.isEmpty()) {
                // nếu bạn dùng room=conversationId thì có thể: emitter.emitToRoom(conv.getId(), "message:new", dto);
                emitter.emitToUsers(remain, "message", dto);
            }
            // b) Cho người rời: loại bỏ hội thoại khỏi list
            emitter.emitToUser(leaverId, "leave",
                    Map.of("conversationId", conversationId));

            // c) (tuỳ chọn) Nếu có thay đổi quyền admin, báo cho nhóm cập nhật role
            if (promotedFinal != null) {
                // thông báo để FE refresh member list/role
                Map<String, Object> payload = Map.of(
                        "conversationId", conversationId,
                        "userId", promotedFinal,
                        "role", "admin"
                );
                if (!remain.isEmpty()) emitter.emitToUsers(remain, "member_role_updated", payload);
            }
        });

        return LeaveGroupResponse.builder()
                .conversationId(conversationId)
                .leaverId(leaverId)
                .promotedToAdmin(promotedUserId)
                .conversationDeleted(false)
                .build();
    }


}
