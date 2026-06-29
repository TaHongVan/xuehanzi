package com.hanzii.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
@RequiredArgsConstructor
public class RealtimeService {

    @Value("${app.openai.api-key:}")
    private String apiKey;

    @Value("${app.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${app.openai.realtime-model:gpt-realtime-2}")
    private String realtimeModel;

    @Value("${app.openai.realtime-voice:marin}")
    private String realtimeVoice;

    public String createCall(String sdpOffer) {
        if (!StringUtils.hasText(apiKey) || apiKey.startsWith("sk-your")) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "OPENAI_API_KEY is not configured");
        }
        if (!StringUtils.hasText(sdpOffer)) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Missing SDP offer");
        }

        String sessionConfig = """
                {
                  "type": "realtime",
                  "model": "%s",
                  "instructions": "You are a Chinese speaking practice tutor for Vietnamese learners. Keep each turn short and natural. Use simple HSK1-HSK2 Chinese, include pinyin when helpful, briefly correct mistakes in Vietnamese, and always continue with one short follow-up question.",
                  "audio": {
                    "output": {
                      "voice": "%s"
                    }
                  }
                }
                """.formatted(realtimeModel, realtimeVoice);

        try {
            String boundary = "hanzii-" + UUID.randomUUID();
            byte[] body = multipartBody(boundary, sdpOffer, sessionConfig);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/realtime/calls"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("OpenAI-Safety-Identifier", "hanzii-user")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(
                        BAD_GATEWAY,
                        "OpenAI Realtime error: " + response.statusCode() + " - " + response.body());
            }

            return response.body();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(BAD_GATEWAY, "Could not create realtime session", e);
        }
    }

    private byte[] multipartBody(String boundary, String sdpOffer, String sessionConfig) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writePart(out, boundary, "sdp", "application/sdp", sdpOffer);
        writePart(out, boundary, "session", "application/json", sessionConfig);
        out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private void writePart(ByteArrayOutputStream out, String boundary, String name, String contentType, String value)
            throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(value.getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
