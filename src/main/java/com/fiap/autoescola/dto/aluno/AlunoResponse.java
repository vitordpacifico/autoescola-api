package com.fiap.autoescola.dto.aluno;

import com.fiap.autoescola.model.Aluno;

public record AlunoResponse(Long id, String nome, String email, String cpf) {
    public static AlunoResponse from(Aluno aluno) {
        return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getEmail(), aluno.getCpf());
    }
}
