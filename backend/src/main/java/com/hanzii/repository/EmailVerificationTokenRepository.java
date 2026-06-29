package com.hanzii.repository;

import com.hanzii.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);
}
