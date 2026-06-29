package com.hanzii.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ChineseWordSegmenter {

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "[\\u4e00-\\u9fff]+|[^\\u4e00-\\u9fff\\s]");

    private ChineseWordSegmenter() {}

    public static List<String> segment(String sentence, Set<String> knownWords) {
        List<String> sortedWords = knownWords.stream()
                .filter(w -> w != null && !w.isBlank())
                .sorted(Comparator.comparingInt(String::length).reversed())
                .collect(Collectors.toList());

        List<String> result = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(sentence);

        while (matcher.find()) {
            String token = matcher.group();
            if (isChinese(token)) {
                result.addAll(segmentChinesePart(token, sortedWords));
            } else {
                result.add(token);
            }
        }
        return result.isEmpty() ? List.of(sentence) : result;
    }

    private static List<String> segmentChinesePart(String text, List<String> knownWords) {
        List<String> parts = new ArrayList<>();
        int index = 0;

        while (index < text.length()) {
            String matched = null;
            int maxLen = Math.min(6, text.length() - index);

            for (int len = maxLen; len >= 1; len--) {
                String candidate = text.substring(index, index + len);
                if (knownWords.contains(candidate)) {
                    matched = candidate;
                    break;
                }
            }

            if (matched != null) {
                parts.add(matched);
                index += matched.length();
            } else {
                parts.add(String.valueOf(text.charAt(index)));
                index++;
            }
        }
        return parts;
    }

    private static boolean isChinese(String text) {
        return text.chars().allMatch(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN);
    }
}
