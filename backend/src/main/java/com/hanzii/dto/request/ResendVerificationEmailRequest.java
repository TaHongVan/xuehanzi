package com.hanzii.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendVerificationEmailRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;
}
