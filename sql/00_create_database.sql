IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'hanzii_db')
BEGIN
    CREATE DATABASE hanzii_db
    COLLATE Vietnamese_CI_AS;
END
GO
