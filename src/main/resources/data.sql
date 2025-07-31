


INSERT INTO users (id, email, password_hash, username, avatar_url, status, created_at, updated_at)
VALUES
    ('u1', 'alice@example.com', 'hashed_pw1', 'Alice', 'https://avatar.com/alice.png', TRUE, NOW(), NOW()),
    ('u2', 'bob@example.com', 'hashed_pw2', 'Bob', 'https://avatar.com/bob.png', TRUE, NOW(), NOW()),
    ('u3', 'carol@example.com', 'hashed_pw3', 'Carol', NULL, TRUE, NOW(), NOW());

INSERT INTO conversation (id, type, name, avatar, created_by, created_at)
VALUES
    ('c1', 'direct', NULL, NULL, 'u1', NOW()),
    ('c2', 'group', 'Group Chat', 'https://group.avatar/img.png', 'u2', NOW());

INSERT INTO friend (id, requester_id, addressee_id, status, created_at)
VALUES
    ('f1', 'u1', 'u2', 'accepted', NOW()),
    ('f2', 'u1', 'u3', 'pending', NOW());

INSERT INTO blocked_user (id, user_id, blocked_user_id, blocked_at)
VALUES
    ('b1', 'u3', 'u2', NOW());

INSERT INTO member (id, conversation_id, user_id, role, joined_at)
VALUES
    ('m1', 'c2', 'u1', 'admin', NOW()),
    ('m2', 'c2', 'u2', 'member', NOW());

INSERT INTO message (id, conversation_id, sender_id, type, content, status, timestamp)
VALUES
    ('msg1', 'c2', 'u1', 'text', 'Hello team!', 'read', NOW()),
    ('msg2', 'c2', 'u2', 'image', 'Check this out', 'delivered', NOW());

INSERT INTO file (id, message_id, type, fileName, url, path, md5checksum)
VALUES
    ('f01', 'msg2', 'image', 'image1.png', 'https://files.com/image1.png', '/files/image1.png', 'md5dummyhash');

INSERT INTO message_reaction (id, message_id, user_id, emoji, reacted_at)
VALUES
    ('r1', 'msg1', 'u2', '👍', NOW());

INSERT INTO read_receipt (id, message_id, user_id, read_at, status)
VALUES
    ('rr1', 'msg1', 'u2', NOW(), TRUE);

INSERT INTO verify_code (id, user_id, code, expires_at, status, created_at)
VALUES
    ('v1', 'u1', 'ABC123', DATE_ADD(NOW(), INTERVAL 1 DAY), TRUE, NOW());

INSERT INTO user_session (id, sessionId, created_at, expires_at)
VALUES
    ('u1', 'sess123', NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY));

INSERT INTO invalid_token (token, created_at, expires_at)
VALUES
    ('invalidtok123', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY));

INSERT INTO notification (id, title, body, type, related_id, created_at)
VALUES
    ('n1', 'New Message', 'You got a new message', 'new_message', 'msg1', NOW());

INSERT INTO user_notification (id, user_id, notification_id, is_read)
VALUES
    ('un1', 'u2', 'n1', FALSE),
    ('un2', 'u1', 'n1', TRUE);
