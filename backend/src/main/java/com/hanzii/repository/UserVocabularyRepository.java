package com.hanzii.repository;

import com.hanzii.entity.UserVocabulary;
import com.hanzii.entity.enums.LearningStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserVocabularyRepository extends JpaRepository<UserVocabulary, Long> {

    @EntityGraph(attributePaths = "vocabulary")
    Optional<UserVocabulary> findByUserIdAndVocabularyId(Long userId, Long vocabularyId);

    @EntityGraph(attributePaths = "vocabulary")
    @Query("SELECT uv FROM UserVocabulary uv WHERE uv.user.id = :userId AND uv.vocabulary.id IN :vocabIds")
    List<UserVocabulary> findByUserIdAndVocabularyIds(@Param("userId") Long userId, @Param("vocabIds") List<Long> vocabIds);

    @EntityGraph(attributePaths = "vocabulary")
    List<UserVocabulary> findByUserIdAndStatus(Long userId, LearningStatus status);

    long countByUserIdAndStatus(Long userId, LearningStatus status);

    long countByUserIdAndUpdatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
