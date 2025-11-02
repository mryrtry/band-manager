package org.is.auth.service.jwt;

import lombok.extern.slf4j.Slf4j;
import org.is.auth.constants.JwtConstants;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, TokenInfo> blacklistedTokens = new ConcurrentHashMap<>();

    private long lastCleanup = System.currentTimeMillis();

    private static class TokenInfo {
        String username;
        long expiresAt;

        TokenInfo(String username, long expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }
    }

    public void blacklistToken(String token, String username, Instant expiresAt) {
        blacklistedTokens.put(token, new TokenInfo(username, expiresAt.toEpochMilli()));
        log.debug("Token for user {} blacklisted, expires at: {}", username, expiresAt);
        cleanupExpiredTokens();
    }

    public boolean isTokenBlacklisted(String token) {
        TokenInfo tokenInfo = blacklistedTokens.get(token);
        if (tokenInfo == null) {
            return false;
        }

        if (System.currentTimeMillis() > tokenInfo.expiresAt) {
            blacklistedTokens.remove(token);
            return false;
        }

        return true;
    }

    public void invalidateToken(String token, String username) {
        TokenInfo tokenInfo = blacklistedTokens.get(token);
        if (tokenInfo != null && username.equals(tokenInfo.username)) {
            blacklistedTokens.remove(token);
            log.debug("Token invalidated for user: {}", username);
        }
    }

    private void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanup < JwtConstants.BLACKLIST_CLEANUP_INTERVAL) {
            return;
        }

        log.debug("Cleaning up expired blacklisted tokens");
        int initialSize = blacklistedTokens.size();

        blacklistedTokens.entrySet().removeIf(entry ->
                System.currentTimeMillis() > entry.getValue().expiresAt
        );

        lastCleanup = currentTime;
        log.debug("Blacklist cleanup: {} -> {} tokens", initialSize, blacklistedTokens.size());
    }

    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

}