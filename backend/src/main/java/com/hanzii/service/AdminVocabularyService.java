package com.hanzii.service;

import com.hanzii.dto.request.VocabularyRequest;
import com.hanzii.dto.response.PageResponse;
import com.hanzii.dto.response.VocabularyResponse;
import com.hanzii.entity.Topic;
import com.hanzii.entity.Vocabulary;
import com.hanzii.exception.ResourceNotFoundException;
import com.hanzii.mapper.VocabularyMapper;
import com.hanzii.repository.TopicRepository;
import com.hanzii.repository.VocabularyRepository;
import com.hanzii.specification.VocabularySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminVocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final TopicRepository topicRepository;
    private final VocabularyMapper vocabularyMapper;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public PageResponse<VocabularyResponse> list(String keyword, Integer hskLevel, Long topicId, int page, int size) {
        Specification<Vocabulary> spec = VocabularySpecification.withFilters(hskLevel, topicId, keyword);
        Page<Vocabulary> result = vocabularyRepository.findAll(
                spec, PageRequest.of(page, size, Sort.by("hskLevel", "id")));

        return PageResponse.<VocabularyResponse>builder()
                .content(result.getContent().stream().map(vocabularyMapper::toResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public VocabularyResponse getById(Long id) {
        return vocabularyMapper.toResponse(findVocabulary(id));
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public VocabularyResponse create(VocabularyRequest request) {
        Topic topic = findTopic(request.getTopicId());
        Vocabulary vocabulary = Vocabulary.builder()
                .chineseWord(request.getChineseWord().trim())
                .pinyin(request.getPinyin().trim())
                .meaning(request.getMeaning().trim())
                .example(request.getExample())
                .hskLevel(request.getHskLevel())
                .topic(topic)
                .build();
        return vocabularyMapper.toResponse(vocabularyRepository.save(vocabulary));
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public VocabularyResponse update(Long id, VocabularyRequest request) {
        Vocabulary vocabulary = findVocabulary(id);
        Topic topic = findTopic(request.getTopicId());
        vocabulary.setChineseWord(request.getChineseWord().trim());
        vocabulary.setPinyin(request.getPinyin().trim());
        vocabulary.setMeaning(request.getMeaning().trim());
        vocabulary.setExample(request.getExample());
        vocabulary.setHskLevel(request.getHskLevel());
        vocabulary.setTopic(topic);
        return vocabularyMapper.toResponse(vocabularyRepository.save(vocabulary));
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void delete(Long id) {
        Vocabulary vocabulary = findVocabulary(id);
        vocabulary.setDeleted(true);
        vocabulary.setDeletedAt(LocalDateTime.now());
        vocabularyRepository.save(vocabulary);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public boolean existsByChineseAndTopic(String chinese, Long topicId) {
        return vocabularyRepository.findByChineseWordAndTopicId(chinese, topicId).isPresent();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Vocabulary upsertFromExcel(String chinese, String pinyin, String meaning, String example,
                                    int hskLevel, Topic topic) {
        return vocabularyRepository.findByChineseWordAndTopicId(chinese, topic.getId())
                .map(existing -> {
                    existing.setPinyin(pinyin);
                    existing.setMeaning(meaning);
                    existing.setExample(example);
                    existing.setHskLevel(hskLevel);
                    existing.setTopic(topic);
                    existing.setDeleted(false);
                    existing.setDeletedAt(null);
                    return vocabularyRepository.save(existing);
                })
                .orElseGet(() -> vocabularyRepository.save(Vocabulary.builder()
                        .chineseWord(chinese)
                        .pinyin(pinyin)
                        .meaning(meaning)
                        .example(example)
                        .hskLevel(hskLevel)
                        .topic(topic)
                        .build()));
    }

    private Vocabulary findVocabulary(Long id) {
        return vocabularyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found: " + id));
    }

    private Topic findTopic(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + id));
    }
}
