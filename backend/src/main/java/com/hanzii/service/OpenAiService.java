package com.hanzii.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final ObjectMapper objectMapper;
    private final LocalAiTutorService localAiTutorService;

    @Value("${app.openai.api-key:}")
    private String apiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    public String chat(String userMessage, List<Map<String, String>> history) {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("sk-your")) {
            return localAiTutorService.respond(userMessage, history);
        }

        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "You are a helpful Chinese language tutor for Vietnamese students. " +
                            "Respond in Vietnamese when explaining, use Chinese examples for vocabulary and grammar. " +
                            "Keep answers concise and educational."));

            if (history != null && !history.isEmpty()) {
                List<Map<String, String>> past = history.size() > 1
                        ? history.subList(0, history.size() - 1)
                        : List.of();
                messages.addAll(past);
            }
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("max_tokens", 1000);

            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("OpenAI API error: {} - {}", response.statusCode(), response.body());
                return localAiTutorService.respond(userMessage, history);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            return content.isBlank() ? localAiTutorService.respond(userMessage, history) : content;
        } catch (Exception e) {
            log.error("OpenAI request failed, using local tutor", e);
            return localAiTutorService.respond(userMessage, history);
        }
    }
}
