package com.hanzii.controller;

import com.hanzii.dto.request.SentenceCheckRequest;
import com.hanzii.dto.response.ApiResponse;
import com.hanzii.dto.response.PageResponse;
import com.hanzii.dto.response.SentenceCheckResponse;
import com.hanzii.dto.response.SentenceExerciseResponse;
import com.hanzii.dto.response.SentenceResponse;
import com.hanzii.service.SentenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sentence")
@RequiredArgsConstructor
public class SentenceController {

    private final SentenceService sentenceService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<SentenceResponse>>> getAllSentences(
            @RequestParam(required = false) Integer hsk,
            @RequestParam(required = false) Long topic,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "chinese") String searchType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        
        PageResponse<SentenceResponse> result = sentenceService.getAllSentences(
                hsk, topic, keyword, searchType, page, size, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/exercises")
    public ResponseEntity<ApiResponse<List<SentenceExerciseResponse>>> getExercises(
            @RequestParam(required = false) Integer hsk,
            @RequestParam(required = false) Long topic) {
        return ResponseEntity.ok(ApiResponse.success(sentenceService.getExercises(hsk, topic)));
    }

    @GetMapping("/exercises/{id}")
    public ResponseEntity<ApiResponse<SentenceExerciseResponse>> getExercise(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(sentenceService.getExercise(id)));
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<SentenceCheckResponse>> check(@Valid @RequestBody SentenceCheckRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sentenceService.checkAnswer(request)));
    }
}
