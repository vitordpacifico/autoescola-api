package com.fiap.autoescola.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalTime;

@ConfigurationProperties(prefix = "autoescola")
public record AgendamentoProperties(Agendamento agendamento, Cancelamento cancelamento) {
    public record Agendamento(
            LocalTime abertura,
            LocalTime fechamento,
            int duracaoMinutos,
            int antecedenciaMinimaMinutos,
            int maxInstrucoesPorAlunoDia
    ) {
    }

    public record Cancelamento(int antecedenciaMinimaHoras) {
    }
}
