package org.is.auth.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.auth.exception.message.AuthErrorMessages;
import org.is.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final TokenBlacklistService tokenBlacklistService;
    @Value("${band-manager.jwt.secret}")
    private String jwtSecret;
    @Value("${band-manager.jwt.access-token.expiration:15m}")
    private Duration accessTokenExpiration;
    @Value("${band-manager.jwt.refresh-token.expiration:7d}")
    private Duration refreshTokenExpiration;

    public String generateAccessToken(String username) {
        return createToken(username, accessTokenExpiration);
    }

    public String generateRefreshToken(String username) {
        return createToken(username, refreshTokenExpiration);
    }

    private String createToken(String username, Duration expiration) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(expiration);

        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String, String> generateTokenPair(String username) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", generateAccessToken(username));
        tokens.put("refresh_token", generateRefreshToken(username));
        return tokens;
    }

    public Map<String, String>  refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new ServiceException(AuthErrorMessages.INVALID_CREDENTIALS, "refresh");
        }
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            throw new ServiceException(AuthErrorMessages.TOKEN_BLACKLISTED, "refresh");
        }
        String username = extractUsername(refreshToken);
        Instant expiresAt = extractExpiration(refreshToken).toInstant();
        tokenBlacklistService.blacklistToken(refreshToken, username, expiresAt);
        return generateTokenPair(username);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws MalformedJwtException {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws MalformedJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Date extractExpiration(String token) throws MalformedJwtException {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) throws MalformedJwtException {
        return extractClaim(token, Claims::getSubject);
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenAlive(String token) {
        return !extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token) {
        try {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Attempt to use blacklisted token");
                return false;
            }
            return isTokenAlive(token);
        } catch (Exception e) {
            return false;
        }
    }

    public void invalidateToken(String token, String username) {
        Instant expiresAt = extractExpiration(token).toInstant();
        tokenBlacklistService.blacklistToken(token, username, expiresAt);
        log.info("Token invalidated for user: {}", username);
    }

}