package com.hanzii;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@SpringBootApplication
public class HanziiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HanziiApplication.class, args);
    }
}
