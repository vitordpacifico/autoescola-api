package com.fiap.autoescola.dto.instrucao;

import com.fiap.autoescola.model.Instrucao;
import com.fiap.autoescola.model.MotivoCancelamento;
import com.fiap.autoescola.model.StatusInstrucao;

import java.time.Instant;
import java.time.LocalDateTime;

public record InstrucaoResponse(
        Long id,
        Long alunoId, String alunoNome,
        Long instrutorId, String instrutorNome,
        LocalDateTime dataHora,
        StatusInstrucao status,
        MotivoCancelamento motivoCancelamento,
        Instant canceladoEm
) {
    public static InstrucaoResponse from(Instrucao i) {
        return new InstrucaoResponse(
                i.getId(),
                i.getAluno().getId(), i.getAluno().getNome(),
                i.getInstrutor().getId(), i.getInstrutor().getNome(),
                i.getDataHora(),
                i.getStatus(),
                i.getMotivoCancelamento(),
                i.getCanceladoEm()
        );
    }
}
