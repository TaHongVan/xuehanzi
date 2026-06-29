package com.hanzii.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SentenceCheckRequest {
    @NotNull(message = "Sentence ID is required")
    private Long sentenceId;

    @NotBlank(message = "Arranged sentence is required")
    private String arrangedSentence;
}
