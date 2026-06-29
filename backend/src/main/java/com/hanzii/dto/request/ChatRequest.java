package com.hanzii.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    private Long conversationId;

    @NotBlank(message = "Message is required")
    private String message;
}
