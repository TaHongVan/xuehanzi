package com.hanzii.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class LocalAiTutorService {

    private static final Pattern CHINESE_TEXT = Pattern.compile("[\\u4e00-\\u9fff]+");

    public String respond(String userMessage) {
        return respond(userMessage, List.of());
    }

    public String respond(String userMessage, List<Map<String, String>> history) {
        String normalized = normalize(userMessage);

        if (isSpeakingPractice(userMessage, history)) {
            return respondForSpeakingPractice(userMessage, history);
        }

        if (containsAny(normalized, "xin chao", "hello", "chao", "nihao", "ni hao") || userMessage.contains("你好")) {
            return """
                    Xin chào! Tôi là gia sư tiếng Trung của HanLearn.

                    Bạn có thể hỏi về từ vựng, ngữ pháp, ví dụ câu, hoặc bấm Luyện nói để hội thoại trực tiếp.

                    你好 (nǐ hǎo) = Xin chào.""";
        }

        if (containsAny(normalized, "ngu phap", "grammar", "hsk1", "hsk 1")) {
            return """
                    Ngữ pháp HSK1 cơ bản:

                    1. Chủ ngữ + Động từ + Tân ngữ
                       我喝水。wǒ hē shuǐ. Tôi uống nước.

                    2. Câu hỏi với 吗:
                       你喜欢茶吗？nǐ xǐ huān chá ma?

                    3. Phủ định với 不:
                       我不去。wǒ bú qù.""";
        }

        String chineseWord = extractChineseWord(userMessage);
        if (chineseWord != null) {
            return explainWord(chineseWord);
        }

        return """
                Tôi đang ở chế độ offline vì backend chưa có OPENAI_API_KEY hợp lệ.

                Tôi vẫn có thể hỗ trợ cơ bản:
                - Tra nghĩa chữ Hán
                - Cho ví dụ HSK1
                - Luyện hội thoại mẫu ở mục Luyện nói

                Nếu muốn AI trả lời tự nhiên như Gemini, hãy cấu hình OPENAI_API_KEY cho backend.""";
    }

    private String respondForSpeakingPractice(String userMessage, List<Map<String, String>> history) {
        String normalized = normalize(userMessage);

        if (isPracticeStart(userMessage, normalized)) {
            if (containsAny(normalized, "nha hang", "goi mon")) {
                return """
                        我是服务员。请问，您要吃什么？
                        Wǒ shì fúwùyuán. Qǐngwèn, nín yào chī shénme?
                        Tôi là nhân viên phục vụ. Xin hỏi, bạn muốn ăn gì?

                        Bạn hãy trả lời bằng tiếng Trung, ví dụ: 我要米饭。""";
            }

            if (containsAny(normalized, "mua sam", "ban hang")) {
                return """
                        欢迎光临！你想买什么？
                        Huānyíng guānglín! Nǐ xiǎng mǎi shénme?
                        Chào mừng bạn! Bạn muốn mua gì?

                        Bạn hãy trả lời bằng tiếng Trung, ví dụ: 我想买茶。""";
            }

            return """
                    你好！你叫什么名字？
                    Nǐ hǎo! Nǐ jiào shénme míngzi?
                    Xin chào! Bạn tên là gì?

                    Bạn hãy trả lời bằng tiếng Trung, ví dụ: 我叫 Nam。""";
        }

        if (userMessage.contains("你在做什么")) {
            return """
                    我在和你练习中文。
                    Wǒ zài hé nǐ liànxí Zhōngwén.
                    Tôi đang luyện tiếng Trung với bạn.

                    你今天想练习什么？可以说：我想练习点菜。
                    Nǐ jīntiān xiǎng liànxí shénme?""";
        }

        if (userMessage.contains("我叫")) {
            return """
                    很高兴认识你！你的中文说得不错。
                    Hěn gāoxìng rènshi nǐ! Nǐ de Zhōngwén shuō de búcuò.
                    Rất vui được gặp bạn! Tiếng Trung của bạn khá tốt.

                    你是哪国人？
                    Nǐ shì nǎ guó rén?""";
        }

        if (userMessage.contains("我要") || userMessage.contains("我想要") || userMessage.contains("我想买")) {
            return """
                    好的，可以。
                    Hǎo de, kěyǐ.
                    Được, có thể.

                    Bạn nói khá ổn. Câu tự nhiên hơn:
                    我想要这个。Wǒ xiǎng yào zhège. Tôi muốn cái này.

                    你还要什么？
                    Nǐ hái yào shénme?""";
        }

        if (userMessage.contains("谢谢")) {
            return """
                    不客气！
                    Bú kèqi!
                    Không có gì!

                    Bây giờ hãy thử nói câu khác: 你喜欢喝茶吗？
                    Nǐ xǐhuān hē chá ma?""";
        }

        if (CHINESE_TEXT.matcher(userMessage).find()) {
            return """
                    我听到了：%s
                    Wǒ tīng dào le.
                    Tôi đã nghe bạn nói câu đó.

                    Nếu muốn nói tự nhiên hơn, hãy dùng mẫu:
                    我想练习中文。Wǒ xiǎng liànxí Zhōngwén.

                    你可以再说一遍吗？
                    Nǐ kěyǐ zài shuō yí biàn ma?""".formatted(userMessage);
        }

        return """
                Mình đang luyện nói tiếng Trung với bạn.

                Hãy thử nói bằng tiếng Trung:
                你好，我想练习中文。
                Nǐ hǎo, wǒ xiǎng liànxí Zhōngwén.

                Hoặc bấm micro và nói một câu ngắn.""";
    }

    private boolean isSpeakingPractice(String userMessage, List<Map<String, String>> history) {
        if (isPracticeStart(userMessage, normalize(userMessage))) return true;

        return history != null && history.stream()
                .map(item -> item.getOrDefault("content", ""))
                .map(this::normalize)
                .anyMatch(content -> containsAny(content,
                        "luyen noi",
                        "hoi thoai",
                        "ai luyen noi",
                        "dong vai",
                        "luyen hoi thoai",
                        "nha hang",
                        "mua sam"));
    }

    private boolean isPracticeStart(String text, String normalized) {
        return containsAny(normalized,
                "luyen noi",
                "hoi thoai",
                "ai luyen noi",
                "ai noi truoc",
                "dong vai",
                "nha hang",
                "mua sam",
                "chao hoi")
                || text.contains("Bạn là AI luyện nói")
                || text.contains("Hãy bắt đầu hội thoại");
    }

    private String explainWord(String word) {
        return switch (word) {
            case "你好" -> "你好 (nǐ hǎo) = Xin chào.\nVí dụ: 你好，我叫 Nam。";
            case "谢谢" -> "谢谢 (xiè xie) = Cảm ơn.\nTrả lời: 不客气 (bú kèqi) = Không có gì.";
            case "再见" -> "再见 (zài jiàn) = Tạm biệt.";
            case "水" -> "水 (shuǐ) = Nước.\nVí dụ: 我想喝水。";
            case "请" -> "请 (qǐng) = Xin mời / Làm ơn.\nVí dụ: 请坐。";
            default -> "Từ/câu: " + word + "\n\nNếu bạn đang luyện nói, hãy nói cả câu ngắn. Ví dụ: 我想练习中文。";
        };
    }

    private String extractChineseWord(String text) {
        var matcher = CHINESE_TEXT.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String normalize(String input) {
        if (input == null) return "";
        String lower = input.toLowerCase(Locale.ROOT);
        return java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd');
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}
