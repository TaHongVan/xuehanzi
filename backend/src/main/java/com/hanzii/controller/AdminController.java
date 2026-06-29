package com.hanzii.controller;

import com.hanzii.dto.request.SegmentRequest;
import com.hanzii.dto.request.SentenceRequest;
import com.hanzii.dto.request.VocabularyRequest;
import com.hanzii.dto.response.ApiResponse;
import com.hanzii.dto.response.ExcelImportResponse;
import com.hanzii.dto.response.PageResponse;
import com.hanzii.dto.response.SentenceResponse;
import com.hanzii.dto.response.VocabularyResponse;
import com.hanzii.service.AdminSentenceService;
import com.hanzii.service.AdminVocabularyService;
import com.hanzii.service.ExcelImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ExcelImportService excelImportService;
    private final AdminVocabularyService adminVocabularyService;
    private final AdminSentenceService adminSentenceService;

    // ==================== Vocabulary ====================

    @GetMapping("/vocabularies")
    public ResponseEntity<ApiResponse<PageResponse<VocabularyResponse>>> listVocabularies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer hsk,
            @RequestParam(required = false) Long topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                adminVocabularyService.list(keyword, hsk, topic, page, size)));
    }

    @GetMapping("/vocabularies/{id}")
    public ResponseEntity<ApiResponse<VocabularyResponse>> getVocabulary(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminVocabularyService.getById(id)));
    }

    @PostMapping("/vocabularies")
    public ResponseEntity<ApiResponse<VocabularyResponse>> createVocabulary(
            @Valid @RequestBody VocabularyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tạo từ vựng thành công",
                adminVocabularyService.create(request)));
    }

    @PutMapping("/vocabularies/{id}")
    public ResponseEntity<ApiResponse<VocabularyResponse>> updateVocabulary(
            @PathVariable Long id, @Valid @RequestBody VocabularyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật từ vựng thành công",
                adminVocabularyService.update(id, request)));
    }

    @DeleteMapping("/vocabularies/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVocabulary(@PathVariable Long id) {
        adminVocabularyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa từ vựng thành công", null));
    }

    @GetMapping("/template")
    public ResponseEntity<Resource> downloadVocabularyTemplate() throws IOException {
        Resource resource = excelImportService.generateVocabularyTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vocabulary_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @PostMapping("/upload-excel")
    public ResponseEntity<ApiResponse<ExcelImportResponse>> uploadVocabularyExcel(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(
                "Import từ vựng hoàn tất", excelImportService.importVocabularyExcel(file)));
    }

    // ==================== Sentences ====================

    @GetMapping("/sentences")
    public ResponseEntity<ApiResponse<PageResponse<SentenceResponse>>> listSentences(
            @RequestParam(required = false) Integer hsk,
            @RequestParam(required = false) Long topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                adminSentenceService.list(hsk, topic, page, size)));
    }

    @GetMapping("/sentences/{id}")
    public ResponseEntity<ApiResponse<SentenceResponse>> getSentence(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminSentenceService.getById(id)));
    }

    @PostMapping("/sentences")
    public ResponseEntity<ApiResponse<SentenceResponse>> createSentence(
            @Valid @RequestBody SentenceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tạo câu thành công",
                adminSentenceService.create(request)));
    }

    @PutMapping("/sentences/{id}")
    public ResponseEntity<ApiResponse<SentenceResponse>> updateSentence(
            @PathVariable Long id, @Valid @RequestBody SentenceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật câu thành công",
                adminSentenceService.update(id, request)));
    }

    @DeleteMapping("/sentences/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSentence(@PathVariable Long id) {
        adminSentenceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa câu thành công", null));
    }

    @PostMapping("/sentences/segment")
    public ResponseEntity<ApiResponse<List<String>>> segmentSentence(
            @Valid @RequestBody SegmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                adminSentenceService.autoSegment(request.getChineseSentence())));
    }

    @GetMapping("/sentence-template")
    public ResponseEntity<Resource> downloadSentenceTemplate() throws IOException {
        Resource resource = excelImportService.generateSentenceTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sentence_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @PostMapping("/upload-sentence-excel")
    public ResponseEntity<ApiResponse<ExcelImportResponse>> uploadSentenceExcel(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(
                "Import câu hoàn tất", excelImportService.importSentenceExcel(file)));
    }
}
