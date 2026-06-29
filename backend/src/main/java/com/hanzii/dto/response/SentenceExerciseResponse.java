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
public class SentenceExerciseResponse {
    private Long sentenceId;
    private String vietnameseSentence;
    private List<String> shuffledWords;
    private Integer hskLevel;
    private String topicName;
}
