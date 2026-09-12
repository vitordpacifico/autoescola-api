package com.fiap.autoescola.dto.instrucao;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendarInstrucaoRequest(
        @NotNull(message = "Aluno é obrigatório") Long alunoId,
        Long instrutorId,
        @NotNull(message = "Data/hora é obrigatória") @Future(message = "Data/hora deve ser no futuro") LocalDateTime dataHora
) {
}
