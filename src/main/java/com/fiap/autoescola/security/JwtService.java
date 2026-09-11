package com.fiap.autoescola.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/** Geracao e validacao de tokens JWT (HS256) usados para autenticar as requisicoes. */
@Component
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String gerarToken(String username, String perfil) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim("perfil", perfil)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public List<String> getAuthorities(String token) {
        String perfil = parseClaims(token).get("perfil", String.class);
        return List.of("ROLE_" + perfil);
    }

    /** Retorna true se o token e valido (assinatura ok e nao expirado). */
    public boolean isValido(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
