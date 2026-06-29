package com.hanzii.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultResponse {
    private boolean correct;
    private String correctAnswer;
    private String feedback;
    private Long vocabularyId;
}
