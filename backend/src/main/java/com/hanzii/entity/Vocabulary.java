package com.hanzii.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "vocabularies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vocabulary extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(name = "chinese_word", nullable = false, length = 50)
    private String chineseWord;

    @Nationalized
    @Column(nullable = false, length = 100)
    private String pinyin;

    @Nationalized
    @Column(nullable = false, length = 500)
    private String meaning;

    @Nationalized
    @Column(length = 1000)
    private String example;

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
