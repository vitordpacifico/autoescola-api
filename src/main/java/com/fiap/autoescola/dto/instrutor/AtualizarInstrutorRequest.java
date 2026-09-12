package com.fiap.autoescola.dto.instrutor;

import com.fiap.autoescola.dto.common.EnderecoDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarInstrutorRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 150) String nome,
        @NotBlank(message = "Telefone é obrigatório") @Pattern(regexp = "^\\+?\\d{10,14}$", message = "Telefone inválido") String telefone,
        @NotNull(message = "Endereço é obrigatório") @Valid EnderecoDto endereco
) {
}
