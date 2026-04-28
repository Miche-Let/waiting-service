package com.michelet.waiting.config;

import org.slf4j.MDC;
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
            // 예시: SecurityContext / MDC / 요청 헤더 등에서 userId 추출
            String userId = MDC.get("userId");
            return userId != null
                    ? Optional.of(UUID.fromString(userId))
                    : Optional.empty();
        };
    }
}