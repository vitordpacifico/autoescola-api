package com.fiap.autoescola.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsIntegrationTest {
    private static final String ORIGEM_PERMITIDA = "http://localhost:3000";
    private static final String ORIGEM_NAO_PERMITIDA = "http://site-malicioso.example";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflight_deOrigemPermitida_retornaCabecalhosCors_semExigirToken() throws Exception {
        mockMvc.perform(options("/api/v1/alunos")
                        .header(HttpHeaders.ORIGIN, ORIGEM_PERMITIDA)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_PERMITIDA))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("Authorization")));
    }

    @Test
    void preflight_deOrigemNaoPermitida_retorna403() throws Exception {
        mockMvc.perform(options("/api/v1/alunos")
                        .header(HttpHeaders.ORIGIN, ORIGEM_NAO_PERMITIDA)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void requisicaoReal_deOrigemPermitida_recebeCabecalhoAllowOrigin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, ORIGEM_PERMITIDA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"inexistente\",\"senha\":\"SenhaQualquer@1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_PERMITIDA));
    }
}
