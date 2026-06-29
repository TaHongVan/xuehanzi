package com.hanzii.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    public void sendRegistrationOtp(String email, String name, String otp) {
        if (!mailEnabled) {
            log.info("Registration OTP for {} <{}>: {}", name, email, otp);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Hanzii registration OTP");
        message.setText("""
                Hi %s,

                Your Hanzii registration OTP is: %s

                This code expires in 10 minutes. If you did not request this, you can ignore this email.
                """.formatted(name, otp));
        mailSender.send(message);
    }
}
