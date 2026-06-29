package com.hanzii.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AiTutorTaskService {

    private final OpenAiService openAiService;

    @Async("applicationTaskExecutor")
    public CompletableFuture<String> chatAsync(String userMessage, List<Map<String, String>> history) {
        return CompletableFuture.completedFuture(openAiService.chat(userMessage, history));
    }
}
