-- ============================================================
-- Add soft delete columns for existing databases
-- ============================================================

USE hanzii_db;
GO

IF COL_LENGTH('dbo.vocabularies', 'deleted') IS NULL
BEGIN
    ALTER TABLE dbo.vocabularies
        ADD deleted BIT NOT NULL CONSTRAINT DF_vocabularies_deleted DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.vocabularies', 'deleted_at') IS NULL
BEGIN
    ALTER TABLE dbo.vocabularies
        ADD deleted_at DATETIME2 NULL;
END
GO

IF COL_LENGTH('dbo.sentences', 'deleted') IS NULL
BEGIN
    ALTER TABLE dbo.sentences
        ADD deleted BIT NOT NULL CONSTRAINT DF_sentences_deleted DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.sentences', 'deleted_at') IS NULL
BEGIN
    ALTER TABLE dbo.sentences
        ADD deleted_at DATETIME2 NULL;
END
GO
