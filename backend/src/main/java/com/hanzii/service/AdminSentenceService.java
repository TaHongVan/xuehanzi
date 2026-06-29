package com.hanzii.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanzii.dto.request.SentenceRequest;
import com.hanzii.dto.response.PageResponse;
import com.hanzii.dto.response.SentenceResponse;
import com.hanzii.entity.Sentence;
import com.hanzii.entity.Topic;
import com.hanzii.entity.Vocabulary;
import com.hanzii.exception.ResourceNotFoundException;
import com.hanzii.repository.SentenceRepository;
import com.hanzii.repository.TopicRepository;
import com.hanzii.repository.VocabularyRepository;
import com.hanzii.specification.VocabularySpecification;
import com.hanzii.specification.SentenceSpecification;
import com.hanzii.util.ChineseWordSegmenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSentenceService {

    private final SentenceRepository sentenceRepository;
    private final TopicRepository topicRepository;
    private final VocabularyRepository vocabularyRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public PageResponse<SentenceResponse> list(Integer hskLevel, Long topicId, int page, int size) {
        Specification<Sentence> spec = SentenceSpecification.withFilters(hskLevel, topicId);
        Page<Sentence> result = sentenceRepository.findAll(
                spec, PageRequest.of(page, size, Sort.by("hskLevel", "id")));

        return PageResponse.<SentenceResponse>builder()
                .content(result.getContent().stream().map(this::toResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public SentenceResponse getById(Long id) {
        return toResponse(findSentence(id));
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public SentenceResponse create(SentenceRequest request) {
        Topic topic = findTopic(request.getTopicId());
        Sentence sentence = Sentence.builder()
                .chineseSentence(request.getChineseSentence().trim())
                .vietnameseSentence(request.getVietnameseSentence().trim())
                .wordSegments(serializeSegments(resolveSegments(request)))
                .hskLevel(request.getHskLevel())
                .topic(topic)
                .build();
        return toResponse(sentenceRepository.save(sentence));
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public SentenceResponse update(Long id, SentenceRequest request) {
        Sentence sentence = findSentence(id);
        Topic topic = findTopic(request.getTopicId());
        sentence.setChineseSentence(request.getChineseSentence().trim());
        sentence.setVietnameseSentence(request.getVietnameseSentence().trim());
        sentence.setWordSegments(serializeSegments(resolveSegments(request)));
        sentence.setHskLevel(request.getHskLevel());
        sentence.setTopic(topic);
        return toResponse(sentenceRepository.save(sentence));
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void delete(Long id) {
        Sentence sentence = findSentence(id);
        sentence.setDeleted(true);
        sentence.setDeletedAt(LocalDateTime.now());
        sentenceRepository.save(sentence);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public boolean existsByChineseSentence(String chineseSentence) {
        return sentenceRepository.findByChineseSentence(chineseSentence).isPresent();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Sentence upsertFromExcel(String chinese, String vietnamese, int hskLevel, Topic topic) {
        List<String> segments = autoSegment(chinese);
        return sentenceRepository.findByChineseSentence(chinese)
                .map(existing -> {
                    existing.setVietnameseSentence(vietnamese);
                    existing.setHskLevel(hskLevel);
                    existing.setTopic(topic);
                    existing.setWordSegments(serializeSegments(segments));
                    existing.setDeleted(false);
                    existing.setDeletedAt(null);
                    return sentenceRepository.save(existing);
                })
                .orElseGet(() -> sentenceRepository.save(Sentence.builder()
                        .chineseSentence(chinese)
                        .vietnameseSentence(vietnamese)
                        .wordSegments(serializeSegments(segments))
                        .hskLevel(hskLevel)
                        .topic(topic)
                        .build()));
    }

    public List<String> autoSegment(String chineseSentence) {
        Set<String> knownWords = vocabularyRepository.findAll(VocabularySpecification.withFilters(null, null, null)).stream()
                .map(Vocabulary::getChineseWord)
                .collect(Collectors.toSet());
        return ChineseWordSegmenter.segment(chineseSentence, knownWords);
    }

    private List<String> resolveSegments(SentenceRequest request) {
        if (request.getWordSegments() != null && !request.getWordSegments().isEmpty()) {
            return request.getWordSegments();
        }
        return autoSegment(request.getChineseSentence().trim());
    }

    private String serializeSegments(List<String> segments) {
        try {
            return objectMapper.writeValueAsString(segments);
        } catch (Exception e) {
            log.error("Failed to serialize word segments", e);
            return "[]";
        }
    }

    private List<String> parseSegments(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private SentenceResponse toResponse(Sentence sentence) {
        return SentenceResponse.builder()
                .id(sentence.getId())
                .chineseSentence(sentence.getChineseSentence())
                .vietnameseSentence(sentence.getVietnameseSentence())
                .wordSegments(parseSegments(sentence.getWordSegments()))
                .hskLevel(sentence.getHskLevel())
                .topicId(sentence.getTopic().getId())
                .topicName(sentence.getTopic().getName())
                .build();
    }

    private Sentence findSentence(Long id) {
        return sentenceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence not found: " + id));
    }

    private Topic findTopic(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + id));
    }
}
