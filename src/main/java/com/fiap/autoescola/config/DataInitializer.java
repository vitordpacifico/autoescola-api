package com.fiap.autoescola.config;

import com.fiap.autoescola.model.Perfil;
import com.fiap.autoescola.model.Usuario;
import com.fiap.autoescola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Garante, no primeiro start, a existencia de pelo menos um usuario ADMIN
 * cadastrado - pre-requisito citado no enunciado do CP4 ("uma tabela
 * usuarios... com pelo menos 1 usuario cadastrado").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.username}")
    private String adminUsername;

    @Value("${admin.default.senha}")
    private String adminSenha;

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByUsername(adminUsername)) {
            return;
        }

        Usuario admin = Usuario.builder()
                .username(adminUsername)
                .senhaHash(passwordEncoder.encode(adminSenha))
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .build();

        usuarioRepository.save(admin);
        log.info("Usuário administrador padrão criado: username='{}'. Troque a senha em produção.", adminUsername);
    }
}
