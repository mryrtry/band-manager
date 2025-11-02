package org.is.bandmanager.config;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware") // Добавь auditorAwareRef
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return new SpringSecurityAuditorAware();
    }

    public static class SpringSecurityAuditorAware implements AuditorAware<String> {

        @Override
        @Nonnull
        public Optional<String> getCurrentAuditor() {
            return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                    .map(authentication -> {
                        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                            return ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername();
                        } else if (authentication.getPrincipal() instanceof String) {
                            return (String) authentication.getPrincipal();
                        }
                        return "system";
                    });
        }
    }

}