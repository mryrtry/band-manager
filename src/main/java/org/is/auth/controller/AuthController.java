package org.is.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.auth.dto.UserDto;
import org.is.auth.dto.request.LoginRequest;
import org.is.auth.dto.request.UserRequest;
import org.is.auth.service.jwt.JwtService;
import org.is.auth.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        UserDto user = userService.create(request);
        Map<String, String> tokens = jwtService.generateTokenPair(user.getUsername());

        request.clearPassword();

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "user", user,
                "tokens", tokens
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDto user = userService.get(request.getUsername());
            Map<String, String> tokens = jwtService.generateTokenPair(request.getUsername());

            log.info("User {} successfully authenticated", request.getUsername());
            request.clearPassword();

            return ResponseEntity.ok(Map.of(
                    "user", user,
                    "tokens", tokens
            ));

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            request.clearPassword();
            throw new BadCredentialsException("Invalid username or password");
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {} - {}", request.getUsername(), e.getMessage());
            request.clearPassword();
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody @NotBlank String refreshToken) {
        log.info("Refreshing token");
        Map<String, String> tokens = jwtService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (token != null) {
            jwtService.invalidateToken(token, username);
        }
        SecurityContextHolder.clearContext();
        log.info("User {} logged out", username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody @NotBlank String token) {
        boolean isValid = jwtService.validateToken(token);
        String username = isValid ? jwtService.extractUsername(token) : null;
        return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "username", username != null ? username : ""
        ));
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring("Bearer ".length());
        }
        return null;
    }

}