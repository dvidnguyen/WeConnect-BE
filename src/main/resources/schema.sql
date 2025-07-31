CREATE TABLE conversation_type (
                                   type VARCHAR(20) PRIMARY KEY
);
INSERT INTO conversation_type VALUES ('direct'), ('group');

CREATE TABLE message_type (
                              type VARCHAR(20) PRIMARY KEY
);
INSERT INTO message_type VALUES ('text'), ('image'), ('file'), ('voice'), ('video'), ('sticker');

CREATE TABLE message_status (
                                status VARCHAR(20) PRIMARY KEY
);
INSERT INTO message_status VALUES ('sent'), ('delivered'), ('read');

CREATE TABLE friend_status (
                               status VARCHAR(20) PRIMARY KEY
);
INSERT INTO friend_status VALUES ('pending'), ('accepted'), ('rejected'), ('blocked');

CREATE TABLE file_type (
                           type VARCHAR(20) PRIMARY KEY
);
INSERT INTO file_type VALUES ('image'), ('video'), ('document'), ('audio'), ('other');

CREATE TABLE notification_type (
                                   type VARCHAR(30) PRIMARY KEY
);
INSERT INTO notification_type VALUES ('friend_request'), ('new_message'), ('group_invite'), ('group_update');

CREATE TABLE member_role (
                             role VARCHAR(20) PRIMARY KEY
);
INSERT INTO member_role VALUES ('admin'), ('member');

-- Bảng chính
CREATE TABLE users (
                       id CHAR(36) PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       username VARCHAR(100) NOT NULL,
                       avatar_url TEXT,
                       status BOOLEAN DEFAULT TRUE,
                       created_at DATETIME,
                       updated_at DATETIME
);

CREATE TABLE conversation (
                              id CHAR(36) PRIMARY KEY,
                              type VARCHAR(20),
                              name VARCHAR(255),
                              avatar TEXT,
                              created_by CHAR(36),
                              created_at DATETIME,
                              FOREIGN KEY (type) REFERENCES conversation_type(type),
                              FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE friend (
                        id CHAR(36) PRIMARY KEY,
                        requester_id CHAR(36),
                        addressee_id CHAR(36),
                        status VARCHAR(20),
                        created_at DATETIME,
                        FOREIGN KEY (requester_id) REFERENCES users(id),
                        FOREIGN KEY (addressee_id) REFERENCES users(id),
                        FOREIGN KEY (status) REFERENCES friend_status(status)
);

CREATE TABLE blocked_user (
                              id CHAR(36) PRIMARY KEY,
                              user_id CHAR(36),
                              blocked_user_id CHAR(36),
                              blocked_at DATETIME,
                              FOREIGN KEY (user_id) REFERENCES users(id),
                              FOREIGN KEY (blocked_user_id) REFERENCES users(id)
);

CREATE TABLE member (
                        id CHAR(36) PRIMARY KEY,
                        conversation_id CHAR(36),
                        user_id CHAR(36),
                        role VARCHAR(20),
                        joined_at DATETIME,
                        FOREIGN KEY (conversation_id) REFERENCES conversation(id),
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (role) REFERENCES member_role(role)
);

CREATE TABLE message (
                         id CHAR(36) PRIMARY KEY,
                         conversation_id CHAR(36),
                         sender_id CHAR(36),
                         type VARCHAR(20),
                         content TEXT,
                         status VARCHAR(20),
                         timestamp DATETIME,
                         FOREIGN KEY (conversation_id) REFERENCES conversation(id),
                         FOREIGN KEY (sender_id) REFERENCES users(id),
                         FOREIGN KEY (type) REFERENCES message_type(type),
                         FOREIGN KEY (status) REFERENCES message_status(status)
);

CREATE TABLE file (
                      id CHAR(36) PRIMARY KEY,
                      message_id CHAR(36),
                      type VARCHAR(20),
                      fileName VARCHAR(255),
                      url TEXT,
                      path TEXT,
                      md5checksum VARCHAR(64),
                      FOREIGN KEY (message_id) REFERENCES message(id),
                      FOREIGN KEY (type) REFERENCES file_type(type)
);

CREATE TABLE message_reaction (
                                  id CHAR(36) PRIMARY KEY,
                                  message_id CHAR(36),
                                  user_id CHAR(36),
                                  emoji VARCHAR(20),
                                  reacted_at DATETIME,
                                  FOREIGN KEY (message_id) REFERENCES message(id),
                                  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE read_receipt (
                              id CHAR(36) PRIMARY KEY,
                              message_id CHAR(36),
                              user_id CHAR(36),
                              read_at DATETIME,
                              status BOOLEAN DEFAULT TRUE,
                              FOREIGN KEY (message_id) REFERENCES message(id),
                              FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE verify_code (
                             id CHAR(36) PRIMARY KEY,
                             user_id CHAR(36),
                             code VARCHAR(255),
                             expires_at DATETIME,
                             status BOOLEAN DEFAULT TRUE,
                             created_at DATETIME,
                             FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_session (
                              id CHAR(36) PRIMARY KEY,
                              sessionId VARCHAR(255),
                              created_at DATETIME,
                              expires_at DATETIME,
                              FOREIGN KEY (id) REFERENCES users(id)
);

CREATE TABLE invalid_token (
                               token CHAR(36) PRIMARY KEY,
                               created_at DATETIME,
                               expires_at DATETIME
);

CREATE TABLE notification (
                              id CHAR(36) PRIMARY KEY,
                              title VARCHAR(255),
                              body TEXT,
                              type VARCHAR(30),
                              related_id CHAR(36),
                              created_at DATETIME,
                              FOREIGN KEY (type) REFERENCES notification_type(type)
);


CREATE TABLE user_notification (
                                   id CHAR(36) PRIMARY KEY,
                                   user_id CHAR(36),
                                   notification_id CHAR(36),
                                   is_read BOOLEAN DEFAULT FALSE,
                                   FOREIGN KEY (user_id) REFERENCES users(id),
                                   FOREIGN KEY (notification_id) REFERENCES notification(id)
);
