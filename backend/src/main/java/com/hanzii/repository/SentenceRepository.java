package com.hanzii.repository;

import com.hanzii.entity.Sentence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SentenceRepository extends JpaRepository<Sentence, Long>, JpaSpecificationExecutor<Sentence> {

    @Override
    @EntityGraph(attributePaths = "topic")
    List<Sentence> findAll(Specification<Sentence> spec);

    @Override
    @EntityGraph(attributePaths = "topic")
    Page<Sentence> findAll(Specification<Sentence> spec, Pageable pageable);

    @EntityGraph(attributePaths = "topic")
    Optional<Sentence> findByChineseSentence(String chineseSentence);

    @EntityGraph(attributePaths = "topic")
    Optional<Sentence> findByIdAndDeletedFalse(Long id);

    boolean existsByIdAndDeletedFalse(Long id);
}
