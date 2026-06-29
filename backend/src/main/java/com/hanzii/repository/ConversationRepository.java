package com.hanzii.repository;

import com.hanzii.entity.Conversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @EntityGraph(attributePaths = "messages")
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "messages")
    Optional<Conversation> findByIdAndUserId(Long id, Long userId);
}
