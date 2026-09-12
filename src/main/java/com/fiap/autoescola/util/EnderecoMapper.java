package com.fiap.autoescola.util;

import com.fiap.autoescola.dto.common.EnderecoDto;
import com.fiap.autoescola.model.Endereco;

public final class EnderecoMapper {
    private EnderecoMapper() {
    }

    public static Endereco toEntity(EnderecoDto dto) {
        return new Endereco(dto.logradouro(), dto.numero(), dto.complemento(), dto.bairro(), dto.cidade(), dto.uf(), dto.cep());
    }

    public static void copyInto(EnderecoDto dto, Endereco entity) {
        entity.setLogradouro(dto.logradouro());
        entity.setNumero(dto.numero());
        entity.setComplemento(dto.complemento());
        entity.setBairro(dto.bairro());
        entity.setCidade(dto.cidade());
        entity.setUf(dto.uf());
        entity.setCep(dto.cep());
    }
}
