package com.hanzii.service;

import com.hanzii.dto.request.HandwritingCheckRequest;
import com.hanzii.dto.response.HandwritingCheckResponse;
import org.springframework.stereotype.Service;

@Service
public class HandwritingService {

    public HandwritingCheckResponse checkHandwriting(HandwritingCheckRequest request) {
        String expected = normalize(request.getExpectedCharacter());
        String recognized = normalize(
                request.getRecognizedCharacter() != null ? request.getRecognizedCharacter() : "");

        boolean hasInput = !recognized.isBlank()
                || (request.getDrawnData() != null && request.getDrawnData().length() > 20);

        if (!hasInput) {
            return HandwritingCheckResponse.builder()
                    .correct(false)
                    .expectedCharacter(expected)
                    .recognizedCharacter("")
                    .confidence(0.0)
                    .feedback("Hãy viết chữ lên canvas trước khi kiểm tra.")
                    .build();
        }

        if (recognized.isBlank()) {
            return HandwritingCheckResponse.builder()
                    .correct(false)
                    .expectedCharacter(expected)
                    .recognizedCharacter("")
                    .confidence(0.3)
                    .feedback("Không nhận dạng được chữ. Hãy viết rõ hơn, nét to và đúng thứ tự.")
                    .build();
        }

        boolean exactMatch = expected.equals(recognized);
        boolean containsMatch = recognized.contains(expected) || expected.contains(recognized);
        boolean correct = exactMatch || containsMatch;
        double confidence = exactMatch ? 0.95 : containsMatch ? 0.7 : 0.2;

        String feedback;
        if (exactMatch) {
            feedback = "Viết đúng rồi! 🎉";
        } else if (containsMatch) {
            feedback = "Gần đúng! Nhận dạng: \"" + recognized + "\" — hãy viết rõ hơn.";
        } else {
            feedback = "Chưa đúng. Bạn viết giống \"" + recognized + "\" nhưng cần viết \"" + expected + "\"";
        }

        return HandwritingCheckResponse.builder()
                .correct(correct)
                .expectedCharacter(expected)
                .recognizedCharacter(recognized)
                .confidence(confidence)
                .feedback(feedback)
                .build();
    }

    private String normalize(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", "");
    }
}
