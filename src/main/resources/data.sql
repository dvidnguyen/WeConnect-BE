-- USER
INSERT INTO USER (id, email, password_hash, username, avatar_url, status, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'alice@example.com', 'hash1', 'Alice', 'https://example.com/avatar1.png', true, NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', 'bob@example.com', 'hash2', 'Bob', 'https://example.com/avatar2.png', true, NOW(), NOW()),
    ('33333333-3333-3333-3333-333333333333', 'charlie@example.com', 'hash3', 'Charlie', NULL, true, NOW(), NOW());

-- FRIEND
INSERT INTO FRIEND (id, requester_id, addressee_id, status, created_at)
VALUES
    (UUID(), '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'ACCEPTED', NOW()),
    (UUID(), '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'PENDING', NOW());

-- BLOCKED_USER
INSERT INTO BLOCKED_USER (id, user_id, blocked_user_id, blocked_at)
VALUES
    (UUID(), '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', NOW());

-- CONVERSATION
INSERT INTO CONVERSATION (id, type, name, avatar, created_by, created_at)
VALUES
    ('conv-0000-0000-0000-000000000001', 'GROUP', 'Project Team', 'https://example.com/group.png', '11111111-1111-1111-1111-111111111111', NOW());

-- MEMBER
INSERT INTO MEMBER (id, conversation_id, user_id, role, joined_at)
VALUES
    (UUID(), 'conv-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'ADMIN', NOW()),
    (UUID(), 'conv-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', 'MEMBER', NOW());

-- MESSAGE
INSERT INTO MESSAGE (id, conversation_id, sender_id, type, content, status, timestamp)
VALUES
    ('msg-0000-0000-0000-000000000001', 'conv-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'TEXT', 'Hello team!', 'SENT', NOW());

-- FILE
INSERT INTO FILE (id, messageId, type, fileName, url, path, md5checksum)
VALUES
    (UUID(), 'msg-0000-0000-0000-000000000001', 'IMAGE', 'image1.png', 'https://example.com/image1.png', '/uploads/image1.png', 'abc123def456ghi789');

-- MESSAGE_REACTION
INSERT INTO MESSAGE_REACTION (id, message_id, user_id, emoji, reacted_at)
VALUES
    (UUID(), 'msg-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', '👍', NOW());

-- READ_RECEIPT
INSERT INTO READ_RECEIPT (id, message_id, user_id, read_at, status)
VALUES
    (UUID(), 'msg-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', NOW(), TRUE);

-- VERIFY_CODE
INSERT INTO VERIFY_CODE (id, user_id, code, expires_at, status, created_at)
VALUES
    (UUID(), '11111111-1111-1111-1111-111111111111', '123456', DATE_ADD(NOW(), INTERVAL 10 MINUTE), TRUE, NOW());

-- USER_SESSION
INSERT INTO USER_SESSION (id, sessionId, created_at, expires_at)
VALUES
    (UUID(), 'session-abc123', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY));

-- INVALID_TOKEN
INSERT INTO INVALID_TOKEN (token, created_at, expires_at)
VALUES
    ('expired-token-xyz', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));

-- NOTIFICATION
INSERT INTO NOTIFICATION (id, user_id, title, body, type, related_id, is_read, created_at)
VALUES
    (UUID(), '11111111-1111-1111-1111-111111111111', 'Welcome', 'Welcome to WeConnect!', 'INFO', NULL, FALSE, NOW());
