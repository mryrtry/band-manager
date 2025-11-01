package org.is.auth.controller;

import jakarta.validation.Valid;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "user", user,
                "tokens", tokens
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDto user = userService.get(request.getUsername());
        Map<String, String> tokens = jwtService.generateTokenPair(request.getUsername());

        log.info("User {} successfully logged in", request.getUsername());

        return ResponseEntity.ok(Map.of(
                "user", user,
                "tokens", tokens
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");
        log.info("Refreshing token");

        String newAccessToken = jwtService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok(Map.of(
                "access_token", newAccessToken
        ));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SecurityContextHolder.clearContext();

        log.info("User {} logged out", username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        boolean isValid = jwtService.validateToken(token);

        return ResponseEntity.ok(Map.of("valid", isValid));
    }

}