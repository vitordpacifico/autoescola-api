package com.fiap.autoescola.controller;

import com.fiap.autoescola.exception.ExternalServiceException;
import com.fiap.autoescola.integration.viacep.ViaCepClient;
import com.fiap.autoescola.integration.viacep.ViaCepResponse;
import com.fiap.autoescola.model.Perfil;
import com.fiap.autoescola.model.Usuario;
import com.fiap.autoescola.repository.UsuarioRepository;
import com.fiap.autoescola.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CepIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ViaCepClient viaCepClient;

    private String token;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        Usuario usuario = Usuario.builder()
                .username("operador.cep").senhaHash(passwordEncoder.encode("Operador@123"))
                .perfil(Perfil.USER).ativo(true).build();
        usuarioRepository.save(usuario);
        token = jwtService.gerarToken(usuario.getUsername(), usuario.getPerfil().name());
    }

    @Test
    void consultar_cepExistente_retorna200ComEndereco() throws Exception {
        when(viaCepClient.buscar("01310100")).thenReturn(Optional.of(new ViaCepResponse(
                "01310-100", "Avenida Paulista", "", "Bela Vista", "São Paulo", "SP", null)));

        mockMvc.perform(get("/api/v1/cep/01310-100").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logradouro").value("Avenida Paulista"))
                .andExpect(jsonPath("$.bairro").value("Bela Vista"))
                .andExpect(jsonPath("$.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.uf").value("SP"));
    }

    @Test
    void consultar_cepInexistente_retorna404() throws Exception {
        when(viaCepClient.buscar("99999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/cep/99999999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void consultar_cepComFormatoInvalido_retorna400SemChamarOServicoExterno() throws Exception {
        mockMvc.perform(get("/api/v1/cep/123").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(viaCepClient);
    }

    @Test
    void consultar_quandoViaCepIndisponivel_retorna502() throws Exception {
        when(viaCepClient.buscar(anyString()))
                .thenThrow(new ExternalServiceException("Serviço de consulta de CEP indisponível no momento.", null));

        mockMvc.perform(get("/api/v1/cep/01310100").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadGateway());
    }

    @Test
    void consultar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/cep/01310100"))
                .andExpect(status().isUnauthorized());
    }
}
