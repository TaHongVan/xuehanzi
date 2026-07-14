-- ============================================================
-- Add one-time password reset links
-- ============================================================

USE hanzii_db;
GO

IF OBJECT_ID('dbo.password_reset_tokens', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.password_reset_tokens (
        id          BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id     BIGINT NOT NULL,
        token_hash  NVARCHAR(64) NOT NULL,
        expires_at  DATETIME2 NOT NULL,
        used        BIT NOT NULL CONSTRAINT DF_password_reset_tokens_used DEFAULT 0,
        created_at  DATETIME2 NOT NULL CONSTRAINT DF_password_reset_tokens_created_at DEFAULT GETUTCDATE(),
        updated_at  DATETIME2 NOT NULL CONSTRAINT DF_password_reset_tokens_updated_at DEFAULT GETUTCDATE(),
        created_by  NVARCHAR(100) NOT NULL CONSTRAINT DF_password_reset_tokens_created_by DEFAULT 'system',
        last_modified_by NVARCHAR(100) NOT NULL CONSTRAINT DF_password_reset_tokens_modified_by DEFAULT 'system',
        CONSTRAINT UQ_password_reset_tokens_hash UNIQUE (token_hash),
        CONSTRAINT FK_password_reset_tokens_user FOREIGN KEY (user_id)
            REFERENCES dbo.users(id) ON DELETE CASCADE
    );

    CREATE INDEX IX_password_reset_tokens_user_used
        ON dbo.password_reset_tokens(user_id, used, created_at DESC);
END
GO
