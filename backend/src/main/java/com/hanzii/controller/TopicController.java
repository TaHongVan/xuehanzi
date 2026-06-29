package com.hanzii.controller;

import com.hanzii.dto.response.ApiResponse;
import com.hanzii.dto.response.TopicResponse;
import com.hanzii.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(topicService.getAllTopics()));
    }
}
