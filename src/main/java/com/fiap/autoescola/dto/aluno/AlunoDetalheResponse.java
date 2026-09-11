package com.fiap.autoescola.dto.aluno;

import com.fiap.autoescola.dto.common.EnderecoDto;
import com.fiap.autoescola.model.Aluno;

public record AlunoDetalheResponse(
        Long id, String nome, String email, String telefone, String cpf, EnderecoDto endereco, boolean ativo
) {
    public static AlunoDetalheResponse from(Aluno a) {
        var e = a.getEndereco();
        return new AlunoDetalheResponse(a.getId(), a.getNome(), a.getEmail(), a.getTelefone(), a.getCpf(),
                new EnderecoDto(e.getLogradouro(), e.getNumero(), e.getComplemento(), e.getBairro(), e.getCidade(), e.getUf(), e.getCep()),
                a.isAtivo());
    }
}
