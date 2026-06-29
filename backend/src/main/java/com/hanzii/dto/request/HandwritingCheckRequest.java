package com.hanzii.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HandwritingCheckRequest {
    @NotBlank(message = "Expected character is required")
    private String expectedCharacter;

    private String recognizedCharacter;

    private String drawnData;
}
