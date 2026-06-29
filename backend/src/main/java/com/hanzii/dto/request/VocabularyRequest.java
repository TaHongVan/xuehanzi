package com.hanzii.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VocabularyRequest {
    @NotBlank(message = "Chinese word is required")
    private String chineseWord;

    @NotBlank(message = "Pinyin is required")
    private String pinyin;

    @NotBlank(message = "Meaning is required")
    private String meaning;

    private String example;

    @NotNull(message = "HSK level is required")
    @Min(1) @Max(6)
    private Integer hskLevel;

    @NotNull(message = "Topic is required")
    private Long topicId;
}
