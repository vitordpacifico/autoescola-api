package com.fiap.autoescola.dto.usuario;

import com.fiap.autoescola.model.Perfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(
        @NotBlank(message = "Username é obrigatório") @Size(min = 3, max = 60) String username,
        @NotBlank(message = "Senha é obrigatória") @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres") String senha,
        @NotNull(message = "Perfil é obrigatório") Perfil perfil
) {
}
