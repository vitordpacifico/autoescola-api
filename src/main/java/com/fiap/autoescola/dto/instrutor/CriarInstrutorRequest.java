package com.fiap.autoescola.dto.instrutor;

import com.fiap.autoescola.dto.common.EnderecoDto;
import com.fiap.autoescola.model.Especialidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CriarInstrutorRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 150) String nome,
        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") @Size(max = 150) String email,
        @NotBlank(message = "Telefone é obrigatório") @Pattern(regexp = "^\\+?\\d{10,14}$", message = "Telefone inválido") String telefone,
        @NotBlank(message = "CNH é obrigatória") @Pattern(regexp = "^\\d{11}$", message = "CNH deve conter 11 dígitos") String cnh,
        @NotNull(message = "Especialidade é obrigatória") Especialidade especialidade,
        @NotNull(message = "Endereço é obrigatório") @Valid EnderecoDto endereco
) {
}
