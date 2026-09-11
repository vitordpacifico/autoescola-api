package com.fiap.autoescola.service;

import com.fiap.autoescola.dto.auth.LoginRequest;
import com.fiap.autoescola.dto.auth.TokenResponse;
import com.fiap.autoescola.exception.InvalidCredentialsException;
import com.fiap.autoescola.model.Usuario;
import com.fiap.autoescola.repository.UsuarioRepository;
import com.fiap.autoescola.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.warn("Tentativa de login com usuário inexistente: '{}'", request.username());
                    return new InvalidCredentialsException("Credenciais inválidas.");
                });

        if (!usuario.isAtivo()) {
            log.warn("Tentativa de login em usuário inativo: '{}'", request.username());
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            log.warn("Falha de login (senha incorreta) para usuário '{}'", request.username());
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        String token = jwtService.gerarToken(usuario.getUsername(), usuario.getPerfil().name());
        log.info("Login bem-sucedido: '{}'", usuario.getUsername());
        return TokenResponse.of(token, jwtService.getExpirationMs());
    }
}
