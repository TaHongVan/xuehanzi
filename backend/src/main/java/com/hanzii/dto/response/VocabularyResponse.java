package com.hanzii.dto.response;

import com.hanzii.entity.enums.LearningStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyResponse {
    private Long id;
    private String chineseWord;
    private String pinyin;
    private String meaning;
    private String example;
    private Integer hskLevel;
    private Long topicId;
    private String topicName;
    private LearningStatus status;
}
