package org.is.auth.config;

import lombok.RequiredArgsConstructor;
import org.is.auth.model.Role;
import org.is.auth.model.User;
import org.is.auth.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin-pass"))
                    .roles(Set.of(Role.ROLE_ADMIN))
                    .build();
            userRepository.save(admin);
        }
    }

}