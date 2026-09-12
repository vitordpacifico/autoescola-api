package com.fiap.autoescola.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {
    @NotBlank(message = "Logradouro é obrigatório")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String logradouro;

    @Size(max = 20)
    @Column(length = 20)
    private String numero;

    @Size(max = 100)
    @Column(length = 100)
    private String complemento;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String cidade;

    @NotBlank(message = "UF é obrigatória")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF deve conter 2 letras")
    @Column(nullable = false, length = 2)
    private String uf;

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido (use 00000-000)")
    @Column(nullable = false, length = 9)
    private String cep;
}
