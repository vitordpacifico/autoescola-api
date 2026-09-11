package com.fiap.autoescola.dto.aluno;

import com.fiap.autoescola.model.Aluno;

/** Formato de listagem: apenas Nome, E-mail e CPF (conforme enunciado). */
public record AlunoResponse(Long id, String nome, String email, String cpf) {
    public static AlunoResponse from(Aluno aluno) {
        return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getEmail(), aluno.getCpf());
    }
}
