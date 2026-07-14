package com.hanzii.service;

import com.hanzii.dto.request.LoginRequest;
import com.hanzii.dto.request.GoogleLoginRequest;
import com.hanzii.dto.request.ForgotPasswordRequest;
import com.hanzii.dto.request.RegisterRequest;
import com.hanzii.dto.request.ResetPasswordRequest;
import com.hanzii.dto.request.ResendVerificationEmailRequest;
import com.hanzii.dto.request.VerifyOtpRequest;
import com.hanzii.dto.request.VerifyRegistrationLinkRequest;
import com.hanzii.dto.response.AuthResponse;
import com.hanzii.entity.EmailVerificationToken;
import com.hanzii.entity.PasswordResetToken;
import com.hanzii.entity.User;
import com.hanzii.entity.enums.AuthProvider;
import com.hanzii.entity.enums.Role;
import com.hanzii.exception.BadRequestException;
import com.hanzii.repository.EmailVerificationTokenRepository;
import com.hanzii.repository.PasswordResetTokenRepository;
import com.hanzii.repository.UserRepository;
import com.hanzii.security.JwtTokenProvider;
import com.hanzii.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int OTP_TTL_MINUTES = 10;
    private static final int PASSWORD_RESET_TTL_MINUTES = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Transactional
    public void requestRegistrationOtp(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String name = request.getName().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        emailVerificationTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .ifPresent(existing -> {
                    existing.setUsed(true);
                    emailVerificationTokenRepository.save(existing);
                });

        String otp = generateOtp();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES))
                .build();
        emailVerificationTokenRepository.save(token);
        emailService.sendRegistrationVerification(email, name, otp, buildVerificationLink(email, otp), OTP_TTL_MINUTES);
    }

    @Transactional
    public AuthResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        EmailVerificationToken token = emailVerificationTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException("OTP not found or expired"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }
        if (!passwordEncoder.matches(request.getOtp(), token.getOtpHash())) {
            throw new BadRequestException("OTP is incorrect");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        return completeRegistration(token);
    }

    @Transactional
    public AuthResponse verifyRegistrationLink(VerifyRegistrationLinkRequest request) {
        LinkVerificationPayload payload = parseVerificationLinkToken(request.getToken());
        VerifyOtpRequest otpRequest = new VerifyOtpRequest();
        otpRequest.setEmail(payload.email());
        otpRequest.setOtp(payload.otp());
        return verifyRegistrationOtp(otpRequest);
    }

    @Transactional
    public void resendRegistrationVerification(ResendVerificationEmailRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        EmailVerificationToken latest = emailVerificationTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException("No pending registration found for this email"));

        latest.setUsed(true);
        emailVerificationTokenRepository.save(latest);

        String otp = generateOtp();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .name(latest.getName())
                .email(email)
                .passwordHash(latest.getPasswordHash())
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES))
                .build();
        emailVerificationTokenRepository.save(token);
        emailService.sendRegistrationVerification(email, token.getName(), otp, buildVerificationLink(email, otp), OTP_TTL_MINUTES);
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.findAllByUserIdAndUsedFalse(user.getId())
                    .forEach(existing -> existing.setUsed(true));

            String rawToken = generateSecureToken();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(hashToken(rawToken))
                    .expiresAt(LocalDateTime.now().plusMinutes(PASSWORD_RESET_TTL_MINUTES))
                    .build();
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordReset(
                    user.getEmail(),
                    user.getDisplayName(),
                    buildPasswordResetLink(rawToken),
                    PASSWORD_RESET_TTL_MINUTES);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedFalse(hashToken(request.getToken()))
                .orElseThrow(() -> new BadRequestException("Reset link is invalid or has expired"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetToken.setUsed(true);
            throw new BadRequestException("Reset link is invalid or has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        resetToken.setUsed(true);
        userRepository.save(user);

        passwordResetTokenRepository.findAllByUserIdAndUsedFalse(user.getId())
                .forEach(existing -> existing.setUsed(true));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsername().trim();
        String email = normalizeEmail(identifier);
        User user = userRepository.findByUsernameOrEmail(identifier, email)
                .orElseThrow(() -> new BadRequestException("Invalid email/username or password"));
        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email/username or password");
        }
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleTokenVerifier.GoogleUser googleUser = googleTokenVerifier.verify(request.getIdToken());
        User user = userRepository.findByEmail(googleUser.email())
                .map(existing -> updateGoogleUser(existing, googleUser))
                .orElseGet(() -> createGoogleUser(googleUser));
        return buildAuthResponse(user);
    }

    private User updateGoogleUser(User user, GoogleTokenVerifier.GoogleUser googleUser) {
        user.setEmailVerified(true);
        user.setProviderId(googleUser.subject());
        if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            user.setDisplayName(googleUser.name());
        }
        if (user.getAuthProvider() == null) {
            user.setAuthProvider(AuthProvider.GOOGLE);
        }
        return userRepository.save(user);
    }

    private User createGoogleUser(GoogleTokenVerifier.GoogleUser googleUser) {
        User user = User.builder()
                .username(generateUsername(googleUser.email()))
                .displayName(googleUser.name())
                .email(googleUser.email())
                .emailVerified(true)
                .password(null)
                .role(Role.USER)
                .authProvider(AuthProvider.GOOGLE)
                .providerId(googleUser.subject())
                .build();
        return userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        return AuthResponse.builder()
                .token(tokenProvider.generateToken(principal))
                .username(principal.getUsername())
                .email(principal.getEmail())
                .displayName(user.getDisplayName())
                .role(principal.getRole())
                .userId(principal.getId())
                .build();
    }

    private AuthResponse completeRegistration(EmailVerificationToken token) {
        token.setUsed(true);
        emailVerificationTokenRepository.save(token);

        User user = User.builder()
                .username(generateUsername(token.getEmail()))
                .displayName(token.getName())
                .email(token.getEmail())
                .emailVerified(true)
                .password(token.getPasswordHash())
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .build();
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String buildPasswordResetLink(String token) {
        return UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path("/reset-password")
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private String buildVerificationLink(String email, String otp) {
        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString((email + ":" + otp).getBytes(StandardCharsets.UTF_8));
        return UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path("/register/verify")
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private LinkVerificationPayload parseVerificationLinkToken(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            int separator = decoded.lastIndexOf(':');
            if (separator <= 0 || separator == decoded.length() - 1) {
                throw new BadRequestException("Verification link is invalid");
            }
            return new LinkVerificationPayload(
                    normalizeEmail(decoded.substring(0, separator)),
                    decoded.substring(separator + 1)
            );
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Verification link is invalid");
        }
    }

    private record LinkVerificationPayload(String email, String otp) {
    }

    private String generateUsername(String email) {
        String base = email.substring(0, email.indexOf('@'))
                .replaceAll("[^a-zA-Z0-9_-]", "")
                .toLowerCase();
        if (base.length() < 3) {
            base = "user" + base;
        }
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
            if (candidate.length() > 50) {
                candidate = candidate.substring(0, 50);
            }
        }
        return candidate;
    }
}
