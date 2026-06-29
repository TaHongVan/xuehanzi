package com.hanzii.entity;

import com.hanzii.entity.enums.LearningStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_vocabularies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "vocabulary_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVocabulary extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LearningStatus status;

}
