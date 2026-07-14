-- ============================================================
-- Hanzii - Chinese Learning Platform
-- SQL Server Database Schema
-- ============================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'hanzii_db')
BEGIN
    CREATE DATABASE hanzii_db
    COLLATE Vietnamese_CI_AS;
END
GO

USE hanzii_db;
GO

-- ============================================================
-- Topics
-- ============================================================
IF OBJECT_ID('dbo.topics', 'U') IS NOT NULL DROP TABLE dbo.topics;
CREATE TABLE dbo.topics (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(100)  NOT NULL,
    description NVARCHAR(500)  NULL,
    created_at  DATETIME2      NOT NULL DEFAULT GETUTCDATE(),
    updated_at  DATETIME2      NOT NULL DEFAULT GETUTCDATE(),
    created_by  NVARCHAR(100)  NOT NULL DEFAULT 'system',
    last_modified_by NVARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT UQ_topics_name UNIQUE (name)
);

-- ============================================================
-- Users
-- ============================================================
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
CREATE TABLE dbo.users (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    username   NVARCHAR(50)  NOT NULL,
    display_name NVARCHAR(100) NOT NULL,
    email      NVARCHAR(100) NOT NULL,
    email_verified BIT NOT NULL DEFAULT 0,
    password   NVARCHAR(255) NULL,
    role       NVARCHAR(20)  NOT NULL DEFAULT 'USER',
    auth_provider NVARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id NVARCHAR(100) NULL,
    created_at DATETIME2     NOT NULL DEFAULT GETUTCDATE(),
    updated_at DATETIME2     NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'system',
    last_modified_by NVARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT UQ_users_username UNIQUE (username),
    CONSTRAINT UQ_users_email UNIQUE (email),
    CONSTRAINT CK_users_auth_provider CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),
    CONSTRAINT CK_users_role CHECK (role IN ('USER', 'ADMIN'))
);

-- ============================================================
-- Vocabulary
-- ============================================================
IF OBJECT_ID('dbo.vocabularies', 'U') IS NOT NULL DROP TABLE dbo.vocabularies;
CREATE TABLE dbo.vocabularies (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    chinese_word  NVARCHAR(50)   NOT NULL,
    pinyin        NVARCHAR(100)  NOT NULL,
    meaning       NVARCHAR(500)  NOT NULL,
    example       NVARCHAR(1000) NULL,
    hsk_level     INT            NOT NULL,
    topic_id      BIGINT         NOT NULL,
    created_at    DATETIME2      NOT NULL DEFAULT GETUTCDATE(),
    updated_at    DATETIME2      NOT NULL DEFAULT GETUTCDATE(),
    created_by    NVARCHAR(100)  NOT NULL DEFAULT 'system',
    last_modified_by NVARCHAR(100) NOT NULL DEFAULT 'system',
    deleted       BIT            NOT NULL DEFAULT 0,
    deleted_at    DATETIME2      NULL,
    CONSTRAINT FK_vocabularies_topic FOREIGN KEY (topic_id) REFERENCES dbo.topics(id),
    CONSTRAINT CK_vocabularies_hsk CHECK (hsk_level BETWEEN 1 AND 6)
);

CREATE INDEX IX_vocabularies_hsk_level ON dbo.vocabularies(hsk_level);
CREATE INDEX IX_vocabularies_topic_id  ON dbo.vocabularies(topic_id);

-- ============================================================
-- User Vocabulary (learning progress)
-- ============================================================
IF OBJECT_ID('dbo.user_vocabularies', 'U') IS NOT NULL DROP TABLE dbo.user_vocabularies;
CREATE TABLE dbo.user_vocabularies (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    vocabulary_id  BIGINT        NOT NULL,
    status         NVARCHAR(20)  NOT NULL DEFAULT 'NEW',
    created_at     DATETIME2     NOT NULL DEFAULT GETUTCDATE(),
    updated_at     DATETIME2     NOT NULL DEFAULT GETUTCDATE(),
    created_by     NVARCHAR(100) NOT NULL DEFAULT 'system',
    last_modified_by NVARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT FK_user_vocabularies_user       FOREIGN KEY (user_id)       REFERENCES dbo.users(id) ON DELETE CASCADE,
    CONSTRAINT FK_user_vocabularies_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES dbo.vocabularies(id) ON DELETE CASCADE,
    CONSTRAINT UQ_user_vocabularies_user_vocab UNIQUE (user_id, vocabulary_id),
    CONSTRAINT CK_user_vocabularies_status CHECK (status IN ('NEW', 'LEARNING', 'MASTERED'))
);

CREATE INDEX IX_user_vocabularies_user_id   ON dbo.user_vocabularies(user_id);
CREATE INDEX IX_user_vocabularies_status    ON dbo.user_vocabularies(status);

-- ============================================================
-- Sentences
-- ============================================================
IF OBJECT_ID('dbo.sentences', 'U') IS NOT NULL DROP TABLE dbo.sentences;
CREATE TABLE dbo.sentences (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    chinese_sentence    NVARCHAR(500)  NOT NULL,
    vietnamese_sentence NVARCHAR(500)  NOT NULL,
    hsk_level           INT            NOT NULL,
    topic_id            BIGINT         NOT NULL,
    created_at          DATETIME2      NOT NULL DEFAULT GETUTCDATE(),
    updated_at          DATETIME2      NOT NULL DEFAULT GETUTCDATE(),
    created_by          NVARCHAR(100)  NOT NULL DEFAULT 'system',
    last_modified_by    NVARCHAR(100)  NOT NULL DEFAULT 'system',
    deleted             BIT            NOT NULL DEFAULT 0,
    deleted_at          DATETIME2      NULL,
    CONSTRAINT FK_sentences_topic FOREIGN KEY (topic_id) REFERENCES dbo.topics(id),
    CONSTRAINT CK_sentences_hsk CHECK (hsk_level BETWEEN 1 AND 6)
);

CREATE INDEX IX_sentences_hsk_level ON dbo.sentences(hsk_level);
CREATE INDEX IX_sentences_topic_id  ON dbo.sentences(topic_id);

-- ============================================================
-- Conversations
-- ============================================================
IF OBJECT_ID('dbo.conversations', 'U') IS NOT NULL DROP TABLE dbo.conversations;
CREATE TABLE dbo.conversations (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id    BIGINT        NOT NULL,
    title      NVARCHAR(200) NULL,
    created_at DATETIME2     NOT NULL DEFAULT GETUTCDATE(),
    updated_at DATETIME2     NOT NULL DEFAULT GETUTCDATE(),
    created_by NVARCHAR(100) NOT NULL DEFAULT 'system',
    last_modified_by NVARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT FK_conversations_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE
);

CREATE INDEX IX_conversations_user_id ON dbo.conversations(user_id);

-- ============================================================
-- Messages
-- ============================================================
IF OBJECT_ID('dbo.messages', 'U') IS NOT NULL DROP TABLE dbo.messages;
CREATE TABLE dbo.messages (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    conversation_id BIGINT        NOT NULL,
    sender          NVARCHAR(10)  NOT NULL,
    content         NVARCHAR(MAX) NOT NULL,
    created_at      DATETIME2     NOT NULL DEFAULT GETUTCDATE(),
    updated_at      DATETIME2     NOT NULL DEFAULT GETUTCDATE(),
    created_by      NVARCHAR(100) NOT NULL DEFAULT 'system',
    last_modified_by NVARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT FK_messages_conversation FOREIGN KEY (conversation_id) REFERENCES dbo.conversations(id) ON DELETE CASCADE,
    CONSTRAINT CK_messages_sender CHECK (sender IN ('USER', 'AI'))
);

CREATE INDEX IX_messages_conversation_id ON dbo.messages(conversation_id);

-- ============================================================
-- Email verification tokens
-- ============================================================
IF OBJECT_ID('dbo.email_verification_tokens', 'U') IS NOT NULL DROP TABLE dbo.email_verification_tokens;
CREATE TABLE dbo.email_verification_tokens (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(100) NOT NULL,
    email       NVARCHAR(100) NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    otp_hash    NVARCHAR(255) NOT NULL,
    expires_at  DATETIME2 NOT NULL,
    used        BIT NOT NULL DEFAULT 0,
    created_at  DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    updated_at  DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by  NVARCHAR(100) NOT NULL DEFAULT 'system',
    last_modified_by NVARCHAR(100) NOT NULL DEFAULT 'system'
);

CREATE INDEX IX_email_verification_tokens_email_used
    ON dbo.email_verification_tokens(email, used, created_at DESC);

-- ============================================================
-- Password reset tokens
-- ============================================================
CREATE TABLE dbo.password_reset_tokens (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token_hash  NVARCHAR(64) NOT NULL,
    expires_at  DATETIME2 NOT NULL,
    used        BIT NOT NULL DEFAULT 0,
    created_at  DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    updated_at  DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    created_by  NVARCHAR(100) NOT NULL DEFAULT 'system',
    last_modified_by NVARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT UQ_password_reset_tokens_hash UNIQUE (token_hash),
    CONSTRAINT FK_password_reset_tokens_user FOREIGN KEY (user_id)
        REFERENCES dbo.users(id) ON DELETE CASCADE
);

CREATE INDEX IX_password_reset_tokens_user_used
    ON dbo.password_reset_tokens(user_id, used, created_at DESC);

GO
