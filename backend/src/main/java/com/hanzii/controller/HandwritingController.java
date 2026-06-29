package com.hanzii.controller;

import com.hanzii.dto.request.HandwritingCheckRequest;
import com.hanzii.dto.response.ApiResponse;
import com.hanzii.dto.response.HandwritingCheckResponse;
import com.hanzii.service.HandwritingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/handwriting")
@RequiredArgsConstructor
public class HandwritingController {

    private final HandwritingService handwritingService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<HandwritingCheckResponse>> check(
            @Valid @RequestBody HandwritingCheckRequest request) {
        return ResponseEntity.ok(ApiResponse.success(handwritingService.checkHandwriting(request)));
    }
}
