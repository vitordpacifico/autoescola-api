package com.fiap.autoescola.dto.aluno;

import com.fiap.autoescola.dto.common.EnderecoDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CriarAlunoRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 150) String nome,
        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") @Size(max = 150) String email,
        @NotBlank(message = "Telefone é obrigatório") @Pattern(regexp = "^\\+?\\d{10,14}$", message = "Telefone inválido") String telefone,
        @NotBlank(message = "CPF é obrigatório") @Pattern(regexp = "^\\d{11}$", message = "CPF deve conter 11 dígitos") String cpf,
        @NotNull(message = "Endereço é obrigatório") @Valid EnderecoDto endereco
) {
}
