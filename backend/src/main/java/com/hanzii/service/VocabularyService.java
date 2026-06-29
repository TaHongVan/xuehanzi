package com.hanzii.service;

import com.hanzii.dto.response.PageResponse;
import com.hanzii.dto.response.VocabularyStatsResponse;
import com.hanzii.dto.response.VocabularyResponse;
import com.hanzii.entity.UserVocabulary;
import com.hanzii.entity.Vocabulary;
import com.hanzii.entity.enums.LearningStatus;
import com.hanzii.exception.ResourceNotFoundException;
import com.hanzii.mapper.VocabularyMapper;
import com.hanzii.repository.UserRepository;
import com.hanzii.repository.UserVocabularyRepository;
import com.hanzii.repository.VocabularyRepository;
import com.hanzii.specification.VocabularySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final UserRepository userRepository;
    private final VocabularyMapper vocabularyMapper;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public PageResponse<VocabularyResponse> getVocabularies(
            Long userId, LearningStatus status, Integer hskLevel, Long topicId,
            String keyword, int page, int size) {

        Specification<Vocabulary> spec = Specification
                .where(VocabularySpecification.withFilters(hskLevel, topicId, keyword))
                .and(VocabularySpecification.withStatusFilter(userId, status));

        Page<Vocabulary> result = vocabularyRepository.findAll(
                spec, PageRequest.of(page, size, Sort.by("hskLevel", "id")));

        List<Long> vocabIds = result.getContent().stream().map(Vocabulary::getId).toList();
        Map<Long, LearningStatus> statusMap = getStatusMap(userId, vocabIds);

        List<VocabularyResponse> content = result.getContent().stream()
                .map(v -> vocabularyMapper.toResponseWithStatus(v, statusMap.getOrDefault(v.getId(), LearningStatus.NEW)))
                .toList();

        return PageResponse.<VocabularyResponse>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    @Cacheable(value = "vocabulary", key = "#id")
    public VocabularyResponse getById(Long id, Long userId) {
        Vocabulary vocabulary = vocabularyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found: " + id));
        LearningStatus status = userVocabularyRepository.findByUserIdAndVocabularyId(userId, id)
                .map(UserVocabulary::getStatus)
                .orElse(LearningStatus.NEW);
        return vocabularyMapper.toResponseWithStatus(vocabulary, status);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void updateStatus(Long userId, Long vocabularyId, LearningStatus status) {
        Vocabulary vocabulary = vocabularyRepository.findByIdAndDeletedFalse(vocabularyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found: " + vocabularyId));

        UserVocabulary uv = userVocabularyRepository.findByUserIdAndVocabularyId(userId, vocabularyId)
                .orElse(UserVocabulary.builder()
                        .user(userRepository.getReferenceById(userId))
                        .vocabulary(vocabulary)
                        .status(status)
                        .build());
        uv.setStatus(status);
        userVocabularyRepository.save(uv);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public VocabularyStatsResponse getStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        return VocabularyStatsResponse.builder()
                .masteredCount(userVocabularyRepository.countByUserIdAndStatus(userId, LearningStatus.MASTERED))
                .todayCount(userVocabularyRepository.countByUserIdAndUpdatedAtBetween(userId, startOfDay, startOfNextDay))
                .totalCount(vocabularyRepository.countByDeletedFalse())
                .build();
    }

    private Map<Long, LearningStatus> getStatusMap(Long userId, List<Long> vocabIds) {
        if (vocabIds.isEmpty()) return Map.of();
        return userVocabularyRepository.findByUserIdAndVocabularyIds(userId, vocabIds).stream()
                .collect(Collectors.toMap(uv -> uv.getVocabulary().getId(), UserVocabulary::getStatus));
    }
}
