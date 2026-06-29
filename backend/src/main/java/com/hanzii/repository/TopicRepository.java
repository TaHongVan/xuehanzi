package com.hanzii.repository;

import com.hanzii.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByName(String name);

    @Query("""
            SELECT v.topic.id, COUNT(v.id)
            FROM Vocabulary v
            WHERE v.deleted = false
            GROUP BY v.topic.id
            """)
    List<Object[]> countVocabulariesByTopic();
}
