package com.hanzii.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandwritingCheckResponse {
    private boolean correct;
    private String expectedCharacter;
    private String recognizedCharacter;
    private double confidence;
    private String feedback;
}
