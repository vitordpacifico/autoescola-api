package com.fiap.autoescola.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequest(
        @NotBlank(message = "Senha atual é obrigatória") String senhaAtual,
        @NotBlank(message = "Nova senha é obrigatória") @Size(min = 8, max = 100, message = "Nova senha deve ter entre 8 e 100 caracteres") String novaSenha
) {
}
