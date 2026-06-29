-- Cập nhật tách từ cho bài sắp xếp câu
USE hanzii_db;
GO

-- Thêm cột nếu chưa có (Hibernate ddl-auto cũng tự thêm)
IF COL_LENGTH('dbo.sentences', 'word_segments') IS NULL
BEGIN
    ALTER TABLE dbo.sentences ADD word_segments NVARCHAR(1000) NULL;
END
GO

UPDATE dbo.sentences SET word_segments = N'["你","好吗","？"]' WHERE chinese_sentence = N'你好吗？';
UPDATE dbo.sentences SET word_segments = N'["我","想","喝","一","杯","水","。"]' WHERE chinese_sentence = N'我想喝一杯水。';
UPDATE dbo.sentences SET word_segments = N'["这","是","我的","妈妈","。"]' WHERE chinese_sentence = N'这是我的妈妈。';
UPDATE dbo.sentences SET word_segments = N'["谢谢","你的","帮助","。"]' WHERE chinese_sentence = N'谢谢你的帮助。';
UPDATE dbo.sentences SET word_segments = N'["我","明天","去","北京","。"]' WHERE chinese_sentence = N'我明天去北京。';
UPDATE dbo.sentences SET word_segments = N'["火车","比","飞机","便宜","。"]' WHERE chinese_sentence = N'火车比飞机便宜。';
UPDATE dbo.sentences SET word_segments = N'["我","今天","有","很多","工作","。"]' WHERE chinese_sentence = N'我今天有很多工作。';
UPDATE dbo.sentences SET word_segments = N'["下午","三点","有","会议","。"]' WHERE chinese_sentence = N'下午三点有会议。';
UPDATE dbo.sentences SET word_segments = N'["这里","的","风景","很","美","。"]' WHERE chinese_sentence = N'这里的风景很美。';
UPDATE dbo.sentences SET word_segments = N'["你","有","什么","建议","吗","？"]' WHERE chinese_sentence = N'你有什么建议吗？';
GO
