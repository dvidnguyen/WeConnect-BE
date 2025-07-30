CREATE TABLE USER (
                      id CHAR(36) PRIMARY KEY,
                      email VARCHAR(255) NOT NULL UNIQUE,
                      password_hash VARCHAR(255) NOT NULL,
                      username VARCHAR(100) NOT NULL,
                      avatar_url VARCHAR(512),
                      status BOOLEAN DEFAULT TRUE,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE FRIEND (
                        id CHAR(36) PRIMARY KEY,
                        requester_id CHAR(36),
                        addressee_id CHAR(36),
                        status ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (requester_id) REFERENCES USER(id),
                        FOREIGN KEY (addressee_id) REFERENCES USER(id)
);

CREATE TABLE BLOCKED_USER (
                              id CHAR(36) PRIMARY KEY,
                              user_id CHAR(36),
                              blocked_user_id CHAR(36),
                              blocked_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (user_id) REFERENCES USER(id),
                              FOREIGN KEY (blocked_user_id) REFERENCES USER(id)
);

CREATE TABLE CONVERSATION (
                              id CHAR(36) PRIMARY KEY,
                              type VARCHAR(50),
                              name VARCHAR(100),
                              avatar VARCHAR(255),
                              created_by CHAR(36),
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (created_by) REFERENCES USER(id)
);

CREATE TABLE MEMBER (
                        id CHAR(36) PRIMARY KEY,
                        conversation_id CHAR(36),
                        user_id CHAR(36),
                        role VARCHAR(50),
                        joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (conversation_id) REFERENCES CONVERSATION(id),
                        FOREIGN KEY (user_id) REFERENCES USER(id)
);

CREATE TABLE MESSAGE (
                         id CHAR(36) PRIMARY KEY,
                         conversation_id CHAR(36),
                         sender_id CHAR(36),
                         type VARCHAR(50),
                         content TEXT,
                         status VARCHAR(50),
                         timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (conversation_id) REFERENCES CONVERSATION(id),
                         FOREIGN KEY (sender_id) REFERENCES USER(id)
);

CREATE TABLE FILE (
                      id CHAR(36) PRIMARY KEY,
                      messageId CHAR(36),
                      type VARCHAR(50),
                      fileName VARCHAR(255),
                      url TEXT,
                      path TEXT,
                      md5checksum VARCHAR(64),
                      FOREIGN KEY (messageId) REFERENCES MESSAGE(id)
);

CREATE TABLE MESSAGE_REACTION (
                                  id CHAR(36) PRIMARY KEY,
                                  message_id CHAR(36),
                                  user_id CHAR(36),
                                  emoji VARCHAR(20),
                                  reacted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (message_id) REFERENCES MESSAGE(id),
                                  FOREIGN KEY (user_id) REFERENCES USER(id)
);

CREATE TABLE READ_RECEIPT (
                              id CHAR(36) PRIMARY KEY,
                              message_id CHAR(36),
                              user_id CHAR(36),
                              read_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              status BOOLEAN,
                              FOREIGN KEY (message_id) REFERENCES MESSAGE(id),
                              FOREIGN KEY (user_id) REFERENCES USER(id)
);

CREATE TABLE VERIFY_CODE (
                             id CHAR(36) PRIMARY KEY,
                             user_id CHAR(36),
                             code VARCHAR(10),
                             expires_at DATETIME,
                             status BOOLEAN,
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (user_id) REFERENCES USER(id)
);

CREATE TABLE USER_SESSION (
                              id CHAR(36) PRIMARY KEY,
                              sessionId VARCHAR(255),
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              expires_at DATETIME

);

CREATE TABLE INVALID_TOKEN (
                               token VARCHAR(512) PRIMARY KEY,
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                               expires_at DATETIME
);

CREATE TABLE NOTIFICATION (
                              id CHAR(36) PRIMARY KEY,
                              user_id CHAR(36),
                              title VARCHAR(255),
                              body TEXT,
                              type VARCHAR(50),
                              related_id CHAR(36),
                              is_read BOOLEAN DEFAULT FALSE,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (user_id) REFERENCES USER(id)
);