package com.fiap.autoescola.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.autoescola.dto.auth.LoginRequest;
import com.fiap.autoescola.dto.usuario.AlterarSenhaRequest;
import com.fiap.autoescola.dto.usuario.CriarUsuarioRequest;
import com.fiap.autoescola.model.Perfil;
import com.fiap.autoescola.model.Usuario;
import com.fiap.autoescola.repository.UsuarioRepository;
import com.fiap.autoescola.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre: login/JWT, RBAC (endpoints de usuários só para ADMIN) e troca da
 * própria senha por um usuário comum - o cerne da Atividade do CP4.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthAndRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        Usuario admin = Usuario.builder()
                .username("admin.teste").senhaHash(passwordEncoder.encode("Admin@123456"))
                .perfil(Perfil.ADMIN).ativo(true).build();
        usuarioRepository.save(admin);
        adminToken = jwtService.gerarToken(admin.getUsername(), admin.getPerfil().name());

        Usuario comum = Usuario.builder()
                .username("user.teste").senhaHash(passwordEncoder.encode("User@123456"))
                .perfil(Perfil.USER).ativo(true).build();
        usuarioRepository.save(comum);
        userToken = jwtService.gerarToken(comum.getUsername(), comum.getPerfil().name());
    }

    @Test
    void login_comCredenciaisValidas_retornaToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin.teste", "Admin@123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void login_comSenhaErrada_retorna401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin.teste", "senhaErrada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requisicaoSemToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarUsuarios_comTokenDeAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void listarUsuarios_comTokenDeUsuarioComum_retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void cadastrarUsuario_comTokenDeUsuarioComum_retorna403() throws Exception {
        var request = new CriarUsuarioRequest("novo.usuario", "SenhaForte@123", Perfil.USER);
        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cadastrarUsuario_comTokenDeAdmin_retorna201ComSenhaCriptografada() throws Exception {
        var request = new CriarUsuarioRequest("novo.usuario", "SenhaForte@123", Perfil.USER);
        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("novo.usuario"));

        Usuario salvo = usuarioRepository.findByUsername("novo.usuario").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(salvo.getSenhaHash()).isNotEqualTo("SenhaForte@123");
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("SenhaForte@123", salvo.getSenhaHash())).isTrue();
    }

    @Test
    void usuarioComum_podeAlterarAPropriaSenha() throws Exception {
        var request = new AlterarSenhaRequest("User@123456", "NovaSenhaForte@123");
        mockMvc.perform(put("/api/v1/usuarios/me/senha")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user.teste", "NovaSenhaForte@123"))))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioComum_naoConsegueAlterarSenhaComSenhaAtualErrada() throws Exception {
        var request = new AlterarSenhaRequest("senhaErrada", "NovaSenhaForte@123");
        mockMvc.perform(put("/api/v1/usuarios/me/senha")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
