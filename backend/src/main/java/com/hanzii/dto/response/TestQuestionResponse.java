package com.hanzii.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionResponse {
    private Long vocabularyId;
    private String meaning;
    private Integer hskLevel;
    private String topicName;
}
