package com.hanzii.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class SentenceRequest {
    @NotBlank(message = "Chinese sentence is required")
    private String chineseSentence;

    @NotBlank(message = "Vietnamese sentence is required")
    private String vietnameseSentence;

    private List<String> wordSegments;

    @NotNull(message = "HSK level is required")
    @Min(1) @Max(6)
    private Integer hskLevel;

    @NotNull(message = "Topic is required")
    private Long topicId;
}
