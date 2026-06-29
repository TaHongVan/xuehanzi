-- ============================================================
-- Add Spring Data JPA auditing columns for existing databases
-- ============================================================

USE hanzii_db;
GO

DECLARE @tables TABLE (table_name SYSNAME);
INSERT INTO @tables (table_name)
VALUES
    ('topics'),
    ('users'),
    ('vocabularies'),
    ('user_vocabularies'),
    ('sentences'),
    ('conversations'),
    ('messages');

DECLARE @table SYSNAME;
DECLARE table_cursor CURSOR FOR SELECT table_name FROM @tables;
OPEN table_cursor;
FETCH NEXT FROM table_cursor INTO @table;

WHILE @@FETCH_STATUS = 0
BEGIN
    IF COL_LENGTH('dbo.' + @table, 'created_at') IS NULL
    BEGIN
        EXEC('ALTER TABLE dbo.' + @table + ' ADD created_at DATETIME2 NOT NULL CONSTRAINT DF_' + @table + '_created_at DEFAULT GETUTCDATE()');
    END

    IF COL_LENGTH('dbo.' + @table, 'updated_at') IS NULL
    BEGIN
        EXEC('ALTER TABLE dbo.' + @table + ' ADD updated_at DATETIME2 NOT NULL CONSTRAINT DF_' + @table + '_updated_at DEFAULT GETUTCDATE()');
    END

    IF COL_LENGTH('dbo.' + @table, 'created_by') IS NULL
    BEGIN
        EXEC('ALTER TABLE dbo.' + @table + ' ADD created_by NVARCHAR(100) NOT NULL CONSTRAINT DF_' + @table + '_created_by DEFAULT ''system''');
    END

    IF COL_LENGTH('dbo.' + @table, 'last_modified_by') IS NULL
    BEGIN
        EXEC('ALTER TABLE dbo.' + @table + ' ADD last_modified_by NVARCHAR(100) NOT NULL CONSTRAINT DF_' + @table + '_last_modified_by DEFAULT ''system''');
    END

    FETCH NEXT FROM table_cursor INTO @table;
END

CLOSE table_cursor;
DEALLOCATE table_cursor;
GO
