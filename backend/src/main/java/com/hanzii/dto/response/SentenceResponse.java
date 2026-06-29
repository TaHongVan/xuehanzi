package com.hanzii.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceResponse {
    private Long id;
    private String chineseSentence;
    private String vietnameseSentence;
    private List<String> wordSegments;
    private Integer hskLevel;
    private Long topicId;
    private String topicName;
}
