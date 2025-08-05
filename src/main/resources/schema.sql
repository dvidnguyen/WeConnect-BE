-- Bảng người dùng
CREATE TABLE users (
                       user_id CHAR(255) PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       username VARCHAR(100) NOT NULL,
                       avatar_url TEXT,
                       status BOOLEAN DEFAULT TRUE,
                       created_at DATETIME,
                       updated_at DATETIME
);

-- Bảng cuộc trò chuyện
CREATE TABLE conversation (
                              id CHAR(255) PRIMARY KEY,
                              type VARCHAR(20) CHECK (type IN ('direct', 'group')),
                              name VARCHAR(255),
                              avatar TEXT,
                              created_by CHAR(255),
                              created_at DATETIME,
                              FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Bảng yêu cầu kết bạn (contract) - lưu trạng thái pending
CREATE TABLE contract (
                          id CHAR(255) PRIMARY KEY,
                          requester_user_id CHAR(255),
                          addressee_user_id CHAR(255),
                          created_at DATETIME,
                          FOREIGN KEY (requester_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                          FOREIGN KEY (addressee_user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Thêm chỉ mục cho các trường thường xuyên truy vấn
CREATE INDEX idx_requester_user_id ON contract(requester_user_id);
CREATE INDEX idx_addressee_user_id ON contract(addressee_user_id);

-- Bảng bạn bè (Friend)
CREATE TABLE friend (
                        id CHAR(255) PRIMARY KEY,
                        requester_user_id CHAR(255),
                        addressee_user_id CHAR(255),
                        status VARCHAR(20) CHECK (status IN ('pending', 'accepted', 'rejected', 'blocked')),
                        created_at DATETIME,
                        updated_at DATETIME,
                        FOREIGN KEY (requester_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                        FOREIGN KEY (addressee_user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Thêm chỉ mục cho các trường thường xuyên truy vấn
CREATE INDEX idx_friend_requester_user_id ON friend(requester_user_id);
CREATE INDEX idx_friend_addressee_user_id ON friend(addressee_user_id);

-- Bảng thành viên của cuộc trò chuyện
CREATE TABLE member (
                        id CHAR(255) PRIMARY KEY,
                        conversation_id CHAR(255),
                        user_id CHAR(255),
                        role VARCHAR(20) CHECK (role IN ('admin', 'member')),
                        joined_at DATETIME,
                        FOREIGN KEY (conversation_id) REFERENCES conversation(id),
                        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Bảng tin nhắn
CREATE TABLE message (
                         id CHAR(255) PRIMARY KEY,
                         conversation_id CHAR(255),
                         sender_user_id CHAR(255),
                         type VARCHAR(20) CHECK (type IN ('text', 'image', 'file', 'voice', 'video', 'sticker')),
                         content TEXT,
                         status VARCHAR(20) CHECK (status IN ('sent', 'delivered', 'read')),
                         timestamp DATETIME,
                         FOREIGN KEY (conversation_id) REFERENCES conversation(id),
                         FOREIGN KEY (sender_user_id) REFERENCES users(user_id)
);

-- Bảng tệp đính kèm
CREATE TABLE file (
                      id CHAR(255) PRIMARY KEY,
                      message_id CHAR(255),
                      type VARCHAR(20) CHECK (type IN ('image', 'video', 'document', 'audio', 'other')),
                      fileName VARCHAR(255),
                      url TEXT,
                      path TEXT,
                      md5checksum VARCHAR(64),
                      FOREIGN KEY (message_id) REFERENCES message(id)
);

-- Bảng phản ứng tin nhắn
CREATE TABLE message_reaction (
                                  id CHAR(255),
                                  message_id CHAR(255),
                                  user_id CHAR(255),
                                  emoji VARCHAR(20),
                                  reacted_at DATETIME,
                                  FOREIGN KEY (message_id) REFERENCES message(id),
                                  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Bảng thông báo đã đọc
CREATE TABLE read_receipt (
                              id CHAR(255) PRIMARY KEY,
                              message_id CHAR(255),
                              user_id CHAR(255),
                              read_at DATETIME,
                              status BOOLEAN DEFAULT TRUE,
                              FOREIGN KEY (message_id) REFERENCES message(id),
                              FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Bảng mã xác minh (Verify Code)
CREATE TABLE verify_code (
                             id CHAR(255) PRIMARY KEY,
                             user_id CHAR(255),
                             code VARCHAR(255),
                             expires_at DATETIME,
                             status BOOLEAN DEFAULT TRUE,
                             created_at DATETIME,
                             FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Bảng phiên người dùng
CREATE TABLE user_session (
                              user_id CHAR(255),
                              sessionId VARCHAR(255) PRIMARY KEY,
                              created_at DATETIME,
                              FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Bảng token không hợp lệ
CREATE TABLE invalid_token (
                               token CHAR(36) PRIMARY KEY,
                               created_at DATETIME,
                               expires_at DATETIME
);

-- Bảng thông báo
CREATE TABLE notification (
                              id CHAR(255) PRIMARY KEY,
                              title VARCHAR(255),
                              body TEXT,
                              type VARCHAR(30) CHECK (type IN ('friend_request', 'new_message', 'group_invite', 'group_update')),
                              related_id CHAR(36),
                              created_at DATETIME
);

-- Bảng thông báo của người dùng
CREATE TABLE user_notification (
                                   id CHAR(255) PRIMARY KEY,
                                   user_id CHAR(255),
                                   notification_id CHAR(255),
                                   is_read BOOLEAN DEFAULT FALSE,
                                   FOREIGN KEY (user_id) REFERENCES users(user_id),
                                   FOREIGN KEY (notification_id) REFERENCES notification(id)
);

-- Trigger để xóa yêu cầu từ bảng contract khi yêu cầu được accepted hoặc rejected
DELIMITER //
CREATE TRIGGER remove_pending_contract_after_accept_or_reject
    AFTER UPDATE ON friend
    FOR EACH ROW
BEGIN
    IF NEW.status IN ('accepted', 'rejected') THEN
    DELETE FROM contract
    WHERE (requester_user_id = OLD.requester_user_id AND addressee_user_id = OLD.addressee_user_id)
       OR (requester_user_id = OLD.addressee_user_id AND addressee_user_id = OLD.requester_user_id);
END IF;
END; //
DELIMITER ;

-- Thêm chỉ mục cho các trường thường xuyên truy vấn
CREATE INDEX idx_requester_user_id ON friend(requester_user_id);
CREATE INDEX idx_addressee_user_id ON friend(addressee_user_id);
CREATE INDEX idx_message_sender_user_id ON message(sender_user_id);
CREATE INDEX idx_message_conversation_id ON message(conversation_id);
