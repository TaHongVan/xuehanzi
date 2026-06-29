package com.hanzii.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanzii.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class GoogleTokenVerifier {

    private final ObjectMapper objectMapper;

    @Value("${app.google.client-id:}")
    private String googleClientId;

    public GoogleUser verify(String idToken) {
        try {
            String encodedToken = URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + encodedToken))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BadRequestException("Invalid Google token");
            }

            JsonNode token = objectMapper.readTree(response.body());
            if (StringUtils.hasText(googleClientId) && !googleClientId.equals(token.path("aud").asText())) {
                throw new BadRequestException("Google token audience does not match this app");
            }
            if (!"true".equalsIgnoreCase(token.path("email_verified").asText())) {
                throw new BadRequestException("Google email is not verified");
            }

            return new GoogleUser(
                    token.path("sub").asText(),
                    token.path("email").asText().toLowerCase(),
                    token.path("name").asText(token.path("email").asText()));
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Could not verify Google token");
        }
    }

    public record GoogleUser(String subject, String email, String name) {
    }
}
