package com.hanzii.service;

import com.hanzii.dto.request.LoginRequest;
import com.hanzii.dto.request.GoogleLoginRequest;
import com.hanzii.dto.request.RegisterRequest;
import com.hanzii.dto.request.VerifyOtpRequest;
import com.hanzii.dto.response.AuthResponse;
import com.hanzii.entity.EmailVerificationToken;
import com.hanzii.entity.User;
import com.hanzii.entity.enums.AuthProvider;
import com.hanzii.entity.enums.Role;
import com.hanzii.exception.BadRequestException;
import com.hanzii.repository.EmailVerificationTokenRepository;
import com.hanzii.repository.UserRepository;
import com.hanzii.security.JwtTokenProvider;
import com.hanzii.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int OTP_TTL_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Transactional
    public void requestRegistrationOtp(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String name = request.getName().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        String otp = generateOtp();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES))
                .build();
        emailVerificationTokenRepository.save(token);
        emailService.sendRegistrationOtp(email, name, otp);
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

        token.setUsed(true);
        emailVerificationTokenRepository.save(token);

        User user = User.builder()
                .username(generateUsername(email))
                .displayName(token.getName())
                .email(email)
                .emailVerified(true)
                .password(token.getPasswordHash())
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .build();
        userRepository.save(user);
        return buildAuthResponse(user);
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

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
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
