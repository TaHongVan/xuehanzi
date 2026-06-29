package com.hanzii.service;

import com.hanzii.dto.request.TestSubmitRequest;
import com.hanzii.dto.response.TestQuestionResponse;
import com.hanzii.dto.response.TestResultResponse;
import com.hanzii.entity.UserVocabulary;
import com.hanzii.entity.Vocabulary;
import com.hanzii.entity.enums.LearningStatus;
import com.hanzii.exception.ResourceNotFoundException;
import com.hanzii.repository.UserRepository;
import com.hanzii.repository.UserVocabularyRepository;
import com.hanzii.repository.VocabularyRepository;
import com.hanzii.specification.VocabularySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final VocabularyRepository vocabularyRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TestQuestionResponse> getTestQuestions(Long userId, Integer hskLevel, Long topicId) {
        Specification<Vocabulary> spec = VocabularySpecification.withFilters(hskLevel, topicId, null);
        List<Vocabulary> vocabularies = vocabularyRepository.findAll(spec);

        return vocabularies.stream()
                .filter(v -> !isMastered(userId, v.getId()))
                .map(v -> TestQuestionResponse.builder()
                        .vocabularyId(v.getId())
                        .meaning(v.getMeaning())
                        .hskLevel(v.getHskLevel())
                        .topicName(v.getTopic().getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public TestResultResponse submitAnswer(Long userId, TestSubmitRequest request) {
        Vocabulary vocabulary = vocabularyRepository.findByIdAndDeletedFalse(request.getVocabularyId())
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found"));

        String normalizedAnswer = normalizeChinese(request.getAnswer());
        String correctAnswer = normalizeChinese(vocabulary.getChineseWord());
        boolean correct = normalizedAnswer.equals(correctAnswer);

        if (correct) {
            updateLearningStatus(userId, vocabulary, LearningStatus.MASTERED);
        } else {
            updateLearningStatus(userId, vocabulary, LearningStatus.LEARNING);
        }

        return TestResultResponse.builder()
                .correct(correct)
                .correctAnswer(vocabulary.getChineseWord())
                .feedback(correct ? "Chính xác! 🎉" : "Chưa đúng, hãy thử lại!")
                .vocabularyId(vocabulary.getId())
                .build();
    }

    private boolean isMastered(Long userId, Long vocabId) {
        return userVocabularyRepository.findByUserIdAndVocabularyId(userId, vocabId)
                .map(uv -> uv.getStatus() == LearningStatus.MASTERED)
                .orElse(false);
    }

    private void updateLearningStatus(Long userId, Vocabulary vocabulary, LearningStatus status) {
        UserVocabulary uv = userVocabularyRepository.findByUserIdAndVocabularyId(userId, vocabulary.getId())
                .orElse(UserVocabulary.builder()
                        .user(userRepository.getReferenceById(userId))
                        .vocabulary(vocabulary)
                        .status(status)
                        .build());
        if (uv.getStatus() != LearningStatus.MASTERED) {
            uv.setStatus(status);
        }
        userVocabularyRepository.save(uv);
    }

    private String normalizeChinese(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", "");
    }
}
