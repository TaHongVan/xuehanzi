-- ============================================================
-- Add email/password OTP registration and Google login support
-- ============================================================

USE hanzii_db;
GO

IF COL_LENGTH('dbo.users', 'display_name') IS NULL
BEGIN
    ALTER TABLE dbo.users
        ADD display_name NVARCHAR(100) NOT NULL CONSTRAINT DF_users_display_name DEFAULT 'User';
END
GO

IF COL_LENGTH('dbo.users', 'email') IS NULL
BEGIN
    ALTER TABLE dbo.users
        ADD email NVARCHAR(100) NULL;

    EXEC('UPDATE dbo.users SET email = LOWER(username) + ''@local.hanzii'' WHERE email IS NULL');

    ALTER TABLE dbo.users
        ALTER COLUMN email NVARCHAR(100) NOT NULL;

    ALTER TABLE dbo.users
        ADD CONSTRAINT UQ_users_email UNIQUE (email);
END
GO

IF COL_LENGTH('dbo.users', 'email_verified') IS NULL
BEGIN
    ALTER TABLE dbo.users
        ADD email_verified BIT NOT NULL CONSTRAINT DF_users_email_verified DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.users', 'auth_provider') IS NULL
BEGIN
    ALTER TABLE dbo.users
        ADD auth_provider NVARCHAR(20) NOT NULL CONSTRAINT DF_users_auth_provider DEFAULT 'LOCAL';
END
GO

IF COL_LENGTH('dbo.users', 'provider_id') IS NULL
BEGIN
    ALTER TABLE dbo.users
        ADD provider_id NVARCHAR(100) NULL;
END
GO

ALTER TABLE dbo.users
    ALTER COLUMN password NVARCHAR(255) NULL;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_users_auth_provider'
)
BEGIN
    ALTER TABLE dbo.users
        ADD CONSTRAINT CK_users_auth_provider CHECK (auth_provider IN ('LOCAL', 'GOOGLE'));
END
GO

IF OBJECT_ID('dbo.email_verification_tokens', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.email_verification_tokens (
        id          BIGINT IDENTITY(1,1) PRIMARY KEY,
        name        NVARCHAR(100) NOT NULL,
        email       NVARCHAR(100) NOT NULL,
        password_hash NVARCHAR(255) NOT NULL,
        otp_hash    NVARCHAR(255) NOT NULL,
        expires_at  DATETIME2 NOT NULL,
        used        BIT NOT NULL CONSTRAINT DF_email_verification_tokens_used DEFAULT 0,
        created_at  DATETIME2 NOT NULL CONSTRAINT DF_email_verification_tokens_created_at DEFAULT GETUTCDATE(),
        updated_at  DATETIME2 NOT NULL CONSTRAINT DF_email_verification_tokens_updated_at DEFAULT GETUTCDATE(),
        created_by  NVARCHAR(100) NOT NULL CONSTRAINT DF_email_verification_tokens_created_by DEFAULT 'system',
        last_modified_by NVARCHAR(100) NOT NULL CONSTRAINT DF_email_verification_tokens_last_modified_by DEFAULT 'system'
    );

    CREATE INDEX IX_email_verification_tokens_email_used
        ON dbo.email_verification_tokens(email, used, created_at DESC);
END
GO
