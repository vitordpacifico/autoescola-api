package com.fiap.autoescola.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI autoescolaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auto-Escola API")
                        .description("API REST de agendamento de instruções de uma auto-escola - Checkpoint 5 (SOA e Web Services). "
                                + "Faça login em /api/v1/auth/login e informe o token no botão Authorize.")
                        .version("v1")
                        .contact(new Contact().name("Grupo Auto-Escola - FIAP")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
