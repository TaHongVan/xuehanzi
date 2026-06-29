package com.hanzii.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "sentences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sentence extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(name = "chinese_sentence", nullable = false, length = 500)
    private String chineseSentence;

    @Nationalized
    @Column(name = "vietnamese_sentence", nullable = false, length = 500)
    private String vietnameseSentence;

    @Nationalized
    @Column(name = "word_segments", length = 1000)
    private String wordSegments;

    @Column(name = "hsk_level", nullable = false)
    private Integer hskLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "bit default 0")
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
