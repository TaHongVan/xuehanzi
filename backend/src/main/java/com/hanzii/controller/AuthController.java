package com.hanzii.controller;

import com.hanzii.dto.request.GoogleLoginRequest;
import com.hanzii.dto.request.LoginRequest;
import com.hanzii.dto.request.RegisterRequest;
import com.hanzii.dto.request.VerifyOtpRequest;
import com.hanzii.dto.response.ApiResponse;
import com.hanzii.dto.response.AuthResponse;
import com.hanzii.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> requestRegistrationOtp(@Valid @RequestBody RegisterRequest request) {
        authService.requestRegistrationOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent to email", null));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Registration successful", authService.verifyRegistrationOtp(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Google login successful", authService.googleLogin(request)));
    }
}
