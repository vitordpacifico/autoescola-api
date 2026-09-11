package com.fiap.autoescola.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username é obrigatório") String username,
        @NotBlank(message = "Senha é obrigatória") String senha
) {
}
