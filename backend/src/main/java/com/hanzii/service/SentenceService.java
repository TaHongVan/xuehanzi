package com.hanzii.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanzii.dto.request.SentenceCheckRequest;
import com.hanzii.dto.response.PageResponse;
import com.hanzii.dto.response.SentenceCheckResponse;
import com.hanzii.dto.response.SentenceExerciseResponse;
import com.hanzii.dto.response.SentenceResponse;
import com.hanzii.entity.Sentence;
import com.hanzii.entity.Vocabulary;
import com.hanzii.exception.ResourceNotFoundException;
import com.hanzii.repository.SentenceRepository;
import com.hanzii.repository.VocabularyRepository;
import com.hanzii.specification.SentenceSpecification;
import com.hanzii.specification.VocabularySpecification;
import com.hanzii.util.ChineseWordSegmenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentenceService {

    private final SentenceRepository sentenceRepository;
    private final VocabularyRepository vocabularyRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public List<SentenceExerciseResponse> getExercises(Integer hskLevel, Long topicId) {
        Specification<Sentence> spec = SentenceSpecification.withFilters(hskLevel, topicId);
        return sentenceRepository.findAll(spec).stream()
                .map(this::toExercise)
                .toList();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public SentenceExerciseResponse getExercise(Long id) {
        Sentence sentence = sentenceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence not found: " + id));
        return toExercise(sentence);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public PageResponse<SentenceResponse> getAllSentences(
            Integer hskLevel,
            Long topicId,
            String keyword,
            String searchType,
            int page,
            int size,
            String sortBy,
            String sortOrder) {

        // Xây dựng specification từ các filter
        Specification<Sentence> spec = SentenceSpecification.filter(hskLevel, topicId);

        // Thêm tìm kiếm nếu có keyword
        if (keyword != null && !keyword.isBlank()) {
            if ("vietnamese".equalsIgnoreCase(searchType)) {
                spec = spec.and(SentenceSpecification.searchByVietnamese(keyword));
            } else {
                // Mặc định tìm kiếm theo tiếng Trung
                spec = spec.and(SentenceSpecification.searchByChinese(keyword));
            }
        }

        // Xây dựng sort order
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = "id";
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy.toLowerCase()) {
                case "chinese":
                    sortField = "chineseSentence";
                    break;
                case "vietnamese":
                    sortField = "vietnameseSentence";
                    break;
                case "hsk":
                    sortField = "hskLevel";
                    break;
                case "topic":
                    sortField = "topic";
                    break;
                case "created":
                case "createdat":
                    sortField = "createdAt";
                    break;
                default:
                    sortField = "id";
            }
        }

        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Sentence> sentencePage = sentenceRepository.findAll(spec, pageable);

        List<SentenceResponse> content = sentencePage.getContent().stream()
                .map(this::toSentenceResponse)
                .toList();

        return PageResponse.<SentenceResponse>builder()
                .content(content)
                .page(sentencePage.getNumber())
                .size(sentencePage.getSize())
                .totalElements(sentencePage.getTotalElements())
                .totalPages(sentencePage.getTotalPages())
                .first(sentencePage.isFirst())
                .last(sentencePage.isLast())
                .build();
    }

    public SentenceCheckResponse checkAnswer(SentenceCheckRequest request) {
        Sentence sentence = sentenceRepository.findByIdAndDeletedFalse(request.getSentenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Sentence not found"));

        String normalized = normalizeSentence(request.getArrangedSentence());
        String correct = normalizeSentence(sentence.getChineseSentence());
        boolean isCorrect = normalized.equals(correct);

        return SentenceCheckResponse.builder()
                .correct(isCorrect)
                .correctSentence(sentence.getChineseSentence())
                .feedback(isCorrect ? "Sắp xếp chính xác! 🎉" : "Chưa đúng, hãy thử lại!")
                .build();
    }

    private SentenceExerciseResponse toExercise(Sentence sentence) {
        List<String> words = resolveWordSegments(sentence);
        List<String> shuffled = new ArrayList<>(words);
        Collections.shuffle(shuffled);

        return SentenceExerciseResponse.builder()
                .sentenceId(sentence.getId())
                .vietnameseSentence(sentence.getVietnameseSentence())
                .shuffledWords(shuffled)
                .hskLevel(sentence.getHskLevel())
                .topicName(sentence.getTopic().getName())
                .build();
    }

    private SentenceResponse toSentenceResponse(Sentence sentence) {
        List<String> wordSegments = null;
        if (sentence.getWordSegments() != null && !sentence.getWordSegments().isBlank()) {
            try {
                wordSegments = objectMapper.readValue(
                        sentence.getWordSegments(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse word_segments for sentence {}", sentence.getId());
            }
        }

        return SentenceResponse.builder()
                .id(sentence.getId())
                .chineseSentence(sentence.getChineseSentence())
                .vietnameseSentence(sentence.getVietnameseSentence())
                .wordSegments(wordSegments)
                .hskLevel(sentence.getHskLevel())
                .topicId(sentence.getTopic().getId())
                .topicName(sentence.getTopic().getName())
                .build();
    }

    private List<String> resolveWordSegments(Sentence sentence) {
        if (sentence.getWordSegments() != null && !sentence.getWordSegments().isBlank()) {
            try {
                List<String> segments = objectMapper.readValue(
                        sentence.getWordSegments(), new TypeReference<List<String>>() {});
                if (!segments.isEmpty()) return segments;
            } catch (Exception e) {
                log.warn("Failed to parse word_segments for sentence {}", sentence.getId());
            }
        }
        Set<String> knownWords = vocabularyRepository.findAll(VocabularySpecification.withFilters(null, null, null)).stream()
                .map(Vocabulary::getChineseWord)
                .collect(Collectors.toSet());
        return ChineseWordSegmenter.segment(sentence.getChineseSentence(), knownWords);
    }

    private String normalizeSentence(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", "");
    }
}
