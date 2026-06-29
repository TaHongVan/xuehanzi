-- ============================================================
-- Hanzii - Sample Data
-- Run after 01_schema.sql
-- ============================================================

USE hanzii_db;
GO

-- Admin password: admin123 (BCrypt hash - generated at runtime by app)
-- User password: user123
-- These are placeholder hashes; the app seeder will create real users.

-- ============================================================
-- Topics (5)
-- ============================================================
INSERT INTO dbo.topics (name, description) VALUES
(N'Giao tiếp hàng ngày', N'Các chủ đề giao tiếp cơ bản trong cuộc sống hàng ngày'),
(N'Ăn uống', N'Từ vựng liên quan đến ẩm thực và nhà hàng'),
(N'Du lịch', N'Từ vựng về đi lại, khách sạn, địa điểm'),
(N'Công việc', N'Từ vựng về văn phòng và công việc'),
(N'Gia đình', N'Từ vựng về các thành viên gia đình');

-- ============================================================
-- Vocabulary (20 words, HSK1-HSK3)
-- ============================================================
INSERT INTO dbo.vocabularies (chinese_word, pinyin, meaning, example, hsk_level, topic_id) VALUES
-- HSK1 - Giao tiếp hàng ngày (topic 1)
(N'你好', N'nǐ hǎo', N'Xin chào', N'你好，我是小明。', 1, 1),
(N'谢谢', N'xiè xie', N'Cảm ơn', N'谢谢你的帮助。', 1, 1),
(N'再见', N'zài jiàn', N'Tạm biệt', N'明天见，再见！', 1, 1),
(N'对不起', N'duì bu qǐ', N'Xin lỗi', N'对不起，我迟到了。', 1, 1),
(N'请', N'qǐng', N'Xin mời / Làm ơn', N'请坐。', 1, 1),
-- HSK1 - Ăn uống (topic 2)
(N'水', N'shuǐ', N'Nước', N'我想喝水。', 1, 2),
(N'米饭', N'mǐ fàn', N'Cơm', N'我要一碗米饭。', 1, 2),
(N'茶', N'chá', N'Trà', N'你喜欢喝茶吗？', 1, 2),
-- HSK1 - Gia đình (topic 5)
(N'妈妈', N'mā ma', N'Mẹ', N'我妈妈是老师。', 1, 5),
(N'爸爸', N'bà ba', N'Bố', N'我爸爸很忙。', 1, 5),
-- HSK2 - Du lịch (topic 3)
(N'飞机', N'fēi jī', N'Máy bay', N'我坐飞机去北京。', 2, 3),
(N'火车', N'huǒ chē', N'Tàu hỏa', N'坐火车比坐飞机便宜。', 2, 3),
(N'酒店', N'jiǔ diàn', N'Khách sạn', N'这家酒店很好。', 2, 3),
(N'地图', N'dì tú', N'Bản đồ', N'请给我一张地图。', 2, 3),
-- HSK2 - Công việc (topic 4)
(N'工作', N'gōng zuò', N'Công việc', N'我今天有很多工作。', 2, 4),
(N'会议', N'huì yì', N'Cuộc họp', N'下午三点有会议。', 2, 4),
-- HSK3 - Giao tiếp (topic 1)
(N'建议', N'jiàn yì', N'Đề xuất / Gợi ý', N'你有什么建议吗？', 3, 1),
(N'经验', N'jīng yàn', N'Kinh nghiệm', N'他有很多工作经验。', 3, 4),
-- HSK3 - Du lịch (topic 3)
(N'风景', N'fēng jǐng', N'Phong cảnh', N'这里的风景很美。', 3, 3),
(N'护照', N'hù zhào', N'Hộ chiếu', N'出国需要护照。', 3, 3);

-- ============================================================
-- Sentences (10)
-- ============================================================
INSERT INTO dbo.sentences (chinese_sentence, vietnamese_sentence, hsk_level, topic_id) VALUES
(N'你好吗？', N'Bạn khỏe không?', 1, 1),
(N'我想喝一杯水。', N'Tôi muốn uống một cốc nước.', 1, 2),
(N'这是我的妈妈。', N'Đây là mẹ của tôi.', 1, 5),
(N'谢谢你的帮助。', N'Cảm ơn sự giúp đỡ của bạn.', 1, 1),
(N'我明天去北京。', N'Ngày mai tôi đi Bắc Kinh.', 2, 3),
(N'火车比飞机便宜。', N'Tàu hỏa rẻ hơn máy bay.', 2, 3),
(N'我今天有很多工作。', N'Hôm nay tôi có nhiều việc.', 2, 4),
(N'下午三点有会议。', N'Ba giờ chiều có cuộc họp.', 2, 4),
(N'这里的风景很美。', N'Phong cảnh ở đây rất đẹp.', 3, 3),
(N'你有什么建议吗？', N'Bạn có đề xuất gì không?', 3, 1);

GO
