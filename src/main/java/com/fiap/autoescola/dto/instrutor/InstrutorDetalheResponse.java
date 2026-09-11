package com.fiap.autoescola.dto.instrutor;

import com.fiap.autoescola.dto.common.EnderecoDto;
import com.fiap.autoescola.model.Especialidade;
import com.fiap.autoescola.model.Instrutor;

/** Retornado na criação e na atualização - visão completa de um instrutor. */
public record InstrutorDetalheResponse(
        Long id, String nome, String email, String telefone, String cnh,
        Especialidade especialidade, EnderecoDto endereco, boolean ativo
) {
    public static InstrutorDetalheResponse from(Instrutor i) {
        var e = i.getEndereco();
        return new InstrutorDetalheResponse(i.getId(), i.getNome(), i.getEmail(), i.getTelefone(), i.getCnh(),
                i.getEspecialidade(),
                new EnderecoDto(e.getLogradouro(), e.getNumero(), e.getComplemento(), e.getBairro(), e.getCidade(), e.getUf(), e.getCep()),
                i.isAtivo());
    }
}
