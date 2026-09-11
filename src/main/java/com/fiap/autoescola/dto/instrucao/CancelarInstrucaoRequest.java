package com.fiap.autoescola.dto.instrucao;

import com.fiap.autoescola.model.MotivoCancelamento;
import jakarta.validation.constraints.NotNull;

public record CancelarInstrucaoRequest(
        @NotNull(message = "Motivo do cancelamento é obrigatório") MotivoCancelamento motivo
) {
}
