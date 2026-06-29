package com.hanzii.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SegmentRequest {
    @NotBlank(message = "Chinese sentence is required")
    private String chineseSentence;
}
