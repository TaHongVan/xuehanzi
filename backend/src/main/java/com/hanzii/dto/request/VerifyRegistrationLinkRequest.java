package com.hanzii.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyRegistrationLinkRequest {

    @NotBlank(message = "Verification token is required")
    private String token;
}
