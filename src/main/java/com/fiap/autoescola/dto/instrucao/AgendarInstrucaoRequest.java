package com.fiap.autoescola.dto.instrucao;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * instrutorId é opcional: se omitido, o sistema escolhe aleatoriamente um
 * instrutor disponível na data/hora informada (regra de negócio).
 */
public record AgendarInstrucaoRequest(
        @NotNull(message = "Aluno é obrigatório") Long alunoId,
        Long instrutorId,
        @NotNull(message = "Data/hora é obrigatória") @Future(message = "Data/hora deve ser no futuro") LocalDateTime dataHora
) {
}
