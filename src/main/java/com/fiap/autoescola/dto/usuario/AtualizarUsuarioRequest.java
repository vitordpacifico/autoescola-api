package com.fiap.autoescola.dto.usuario;

import com.fiap.autoescola.model.Perfil;
import jakarta.validation.constraints.NotNull;

/** Atualizacao administrativa do perfil/status de um usuario (apenas ADMIN). */
public record AtualizarUsuarioRequest(
        @NotNull(message = "Perfil é obrigatório") Perfil perfil,
        @NotNull(message = "Status ativo é obrigatório") Boolean ativo
) {
}
