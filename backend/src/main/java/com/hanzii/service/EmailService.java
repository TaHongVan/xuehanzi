package com.hanzii.service;

import com.hanzii.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@hanzii.local}")
    private String mailFrom;

    public void sendRegistrationVerification(String email, String name, String otp, String verificationLink, int expiresInMinutes) {
        if (!mailEnabled) {
            log.info("Mail is disabled. Registration verification for {} <{}>: OTP={}, link={}", name, email, otp, verificationLink);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("Activate your Hanzii account");
            message.setText("""
                    Hi %s,

                    Your Hanzii registration OTP is: %s

                    Or activate your account with this link:
                    %s

                    This verification expires in %d minutes. If you did not request this, you can ignore this email.
                    """.formatted(name, otp, verificationLink, expiresInMinutes));
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send registration verification email to {}", email, ex);
            throw new BadRequestException("Không gửi được email xác thực. Vui lòng kiểm tra cấu hình SMTP.");
        }
    }

    public void sendPasswordReset(String email, String name, String resetLink, int expiresInMinutes) {
        if (!mailEnabled) {
            log.info("Mail is disabled. Password reset for {} <{}>: link={}", name, email, resetLink);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("Reset your Hanzii password");
            message.setText("""
                    Hi %s,

                    Use this link to set a new Hanzii password:
                    %s

                    This link expires in %d minutes and can only be used once.
                    If you did not request a password reset, you can ignore this email.
                    """.formatted(name, resetLink, expiresInMinutes));
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send password reset email to {}", email, ex);
            throw new BadRequestException("Không gửi được email đặt lại mật khẩu. Vui lòng thử lại sau.");
        }
    }
}
