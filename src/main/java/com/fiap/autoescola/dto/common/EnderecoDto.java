package com.fiap.autoescola.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Numero e complemento sao os unicos campos opcionais (regra do enunciado). */
public record EnderecoDto(
        @NotBlank(message = "Logradouro é obrigatório") @Size(max = 150) String logradouro,
        @Size(max = 20) String numero,
        @Size(max = 100) String complemento,
        @NotBlank(message = "Bairro é obrigatório") @Size(max = 100) String bairro,
        @NotBlank(message = "Cidade é obrigatória") @Size(max = 100) String cidade,
        @NotBlank(message = "UF é obrigatória") @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF deve conter 2 letras") String uf,
        @NotBlank(message = "CEP é obrigatório") @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido (use 00000-000)") String cep
) {
}
