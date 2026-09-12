package com.fiap.autoescola.dto.instrutor;

import com.fiap.autoescola.model.Especialidade;
import com.fiap.autoescola.model.Instrutor;

public record InstrutorResponse(Long id, String nome, String email, String cnh, Especialidade especialidade) {
    public static InstrutorResponse from(Instrutor instrutor) {
        return new InstrutorResponse(instrutor.getId(), instrutor.getNome(), instrutor.getEmail(),
                instrutor.getCnh(), instrutor.getEspecialidade());
    }
}
