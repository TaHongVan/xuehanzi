package com.hanzii.config;

import com.hanzii.entity.User;
import com.hanzii.entity.enums.AuthProvider;
import com.hanzii.entity.enums.Role;
import com.hanzii.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDefaultUsers() {
        return args -> {
            createUserIfMissing("admin", "Admin", "admin@local.hanzii", "admin123", Role.ADMIN);
            createUserIfMissing("user", "User", "user@local.hanzii", "user123", Role.USER);
        };
    }

    private void createUserIfMissing(String username, String displayName, String email, String password, Role role) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            return;
        }

        User user = User.builder()
                .username(username)
                .displayName(displayName)
                .email(email)
                .emailVerified(true)
                .password(passwordEncoder.encode(password))
                .role(role)
                .authProvider(AuthProvider.LOCAL)
                .build();
        userRepository.save(user);
    }
}
