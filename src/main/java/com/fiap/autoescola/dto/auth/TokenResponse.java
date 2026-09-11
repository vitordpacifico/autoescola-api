package com.fiap.autoescola.dto.auth;

public record TokenResponse(String accessToken, String tipo, long expiraEmSegundos) {
    public static TokenResponse of(String accessToken, long expirationMs) {
        return new TokenResponse(accessToken, "Bearer", expirationMs / 1000);
    }
}
