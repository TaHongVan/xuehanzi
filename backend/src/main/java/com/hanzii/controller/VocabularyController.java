package com.hanzii.controller;

import com.hanzii.dto.response.ApiResponse;
import com.hanzii.dto.response.PageResponse;
import com.hanzii.dto.response.VocabularyStatsResponse;
import com.hanzii.dto.response.VocabularyResponse;
import com.hanzii.entity.enums.LearningStatus;
import com.hanzii.security.SecurityUtils;
import com.hanzii.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VocabularyResponse>>> list(
            @RequestParam(required = false) LearningStatus status,
            @RequestParam(required = false) Integer hsk,
            @RequestParam(required = false) Long topic,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                vocabularyService.getVocabularies(userId, status, hsk, topic, keyword, page, size)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<VocabularyStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.success(
                vocabularyService.getStats(SecurityUtils.getCurrentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                vocabularyService.getById(id, SecurityUtils.getCurrentUserId())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam LearningStatus status) {
        vocabularyService.updateStatus(SecurityUtils.getCurrentUserId(), id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated", null));
    }
}
