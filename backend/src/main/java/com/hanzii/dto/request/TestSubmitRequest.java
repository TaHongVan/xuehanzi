package com.hanzii.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestSubmitRequest {
    @NotNull(message = "Vocabulary ID is required")
    private Long vocabularyId;

    @NotBlank(message = "Answer is required")
    private String answer;
}
