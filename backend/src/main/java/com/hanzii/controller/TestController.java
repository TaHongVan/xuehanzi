package com.hanzii.controller;

import com.hanzii.dto.request.TestSubmitRequest;
import com.hanzii.dto.response.ApiResponse;
import com.hanzii.dto.response.TestQuestionResponse;
import com.hanzii.dto.response.TestResultResponse;
import com.hanzii.security.SecurityUtils;
import com.hanzii.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<List<TestQuestionResponse>>> getQuestions(
            @RequestParam(required = false) Integer hsk,
            @RequestParam(required = false) Long topic) {
        return ResponseEntity.ok(ApiResponse.success(
                testService.getTestQuestions(SecurityUtils.getCurrentUserId(), hsk, topic)));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<TestResultResponse>> submit(@Valid @RequestBody TestSubmitRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                testService.submitAnswer(SecurityUtils.getCurrentUserId(), request)));
    }
}
