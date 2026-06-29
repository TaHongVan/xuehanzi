package com.hanzii.repository;

import com.hanzii.entity.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Long>, JpaSpecificationExecutor<Vocabulary> {

    @Override
    @EntityGraph(attributePaths = "topic")
    Page<Vocabulary> findAll(Specification<Vocabulary> spec, Pageable pageable);

    @EntityGraph(attributePaths = "topic")
    Optional<Vocabulary> findByChineseWordAndTopicId(String chineseWord, Long topicId);

    @EntityGraph(attributePaths = "topic")
    Optional<Vocabulary> findByIdAndDeletedFalse(Long id);

    boolean existsByIdAndDeletedFalse(Long id);

    long countByDeletedFalse();
}
