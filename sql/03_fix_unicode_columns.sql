-- ============================================================
-- Fix Unicode encoding: convert VARCHAR -> NVARCHAR
-- Run this if Vietnamese/Chinese/Pinyin data appears corrupted
-- ============================================================

USE hanzii_db;
GO

-- Backup note: existing corrupted data cannot be recovered.
-- After running this script, re-run sql/02_sample_data.sql or re-import vocabulary from Excel.

-- Drop FK constraints that block column alterations
ALTER TABLE dbo.vocabularies DROP CONSTRAINT IF EXISTS FK_vocabularies_topic;
ALTER TABLE dbo.sentences DROP CONSTRAINT IF EXISTS FK_sentences_topic;
ALTER TABLE dbo.user_vocabularies DROP CONSTRAINT IF EXISTS FK_user_vocabularies_vocabulary;
GO

-- Topics
ALTER TABLE dbo.topics ALTER COLUMN name NVARCHAR(100) NOT NULL;
ALTER TABLE dbo.topics ALTER COLUMN description NVARCHAR(500) NULL;
GO

-- Vocabularies
ALTER TABLE dbo.vocabularies ALTER COLUMN chinese_word NVARCHAR(50) NOT NULL;
ALTER TABLE dbo.vocabularies ALTER COLUMN pinyin NVARCHAR(100) NOT NULL;
ALTER TABLE dbo.vocabularies ALTER COLUMN meaning NVARCHAR(500) NOT NULL;
ALTER TABLE dbo.vocabularies ALTER COLUMN example NVARCHAR(1000) NULL;
GO

-- Sentences
ALTER TABLE dbo.sentences ALTER COLUMN chinese_sentence NVARCHAR(500) NOT NULL;
ALTER TABLE dbo.sentences ALTER COLUMN vietnamese_sentence NVARCHAR(500) NOT NULL;
GO

-- Conversations & Messages
ALTER TABLE dbo.conversations ALTER COLUMN title NVARCHAR(200) NULL;
ALTER TABLE dbo.messages ALTER COLUMN content NVARCHAR(MAX) NOT NULL;
GO

-- Re-add FK constraints
ALTER TABLE dbo.vocabularies
    ADD CONSTRAINT FK_vocabularies_topic FOREIGN KEY (topic_id) REFERENCES dbo.topics(id);
ALTER TABLE dbo.sentences
    ADD CONSTRAINT FK_sentences_topic FOREIGN KEY (topic_id) REFERENCES dbo.topics(id);
ALTER TABLE dbo.user_vocabularies
    ADD CONSTRAINT FK_user_vocabularies_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES dbo.vocabularies(id);
GO

-- Clear corrupted learning data so it can be re-imported correctly.
DELETE FROM dbo.user_vocabularies;
DELETE FROM dbo.messages;
DELETE FROM dbo.conversations;
DELETE FROM dbo.sentences;
DELETE FROM dbo.vocabularies;
DELETE FROM dbo.topics;
GO

PRINT 'Unicode columns fixed. Re-run sql/02_sample_data.sql or re-import learning data.';
GO
