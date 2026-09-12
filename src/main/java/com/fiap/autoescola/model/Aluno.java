package com.fiap.autoescola.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "alunos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "logradouro", column = @Column(name = "endereco_logradouro", nullable = false, length = 150)),
            @AttributeOverride(name = "numero", column = @Column(name = "endereco_numero", length = 20)),
            @AttributeOverride(name = "complemento", column = @Column(name = "endereco_complemento", length = 100)),
            @AttributeOverride(name = "bairro", column = @Column(name = "endereco_bairro", nullable = false, length = 100)),
            @AttributeOverride(name = "cidade", column = @Column(name = "endereco_cidade", nullable = false, length = 100)),
            @AttributeOverride(name = "uf", column = @Column(name = "endereco_uf", nullable = false, length = 2)),
            @AttributeOverride(name = "cep", column = @Column(name = "endereco_cep", nullable = false, length = 9)),
    })
    private Endereco endereco;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @jakarta.persistence.PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @jakarta.persistence.PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
