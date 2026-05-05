package com.michelet.waiting.config;

import com.michelet.common.auth.webmvc.context.UserContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class AuditorConfig {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            try {
                String userId = UserContextHolder.get().userId();
                if (userId != null && !userId.isBlank()) {
                    return Optional.of(UUID.fromString(userId));
                }
            } catch (RuntimeException ignored) {
            }
            return Optional.empty();
        };
    }
}