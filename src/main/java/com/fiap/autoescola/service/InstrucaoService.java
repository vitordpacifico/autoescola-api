package com.fiap.autoescola.service;

import com.fiap.autoescola.config.AgendamentoProperties;
import com.fiap.autoescola.dto.instrucao.AgendarInstrucaoRequest;
import com.fiap.autoescola.dto.instrucao.CancelarInstrucaoRequest;
import com.fiap.autoescola.dto.instrucao.InstrucaoResponse;
import com.fiap.autoescola.exception.BusinessRuleException;
import com.fiap.autoescola.exception.ResourceNotFoundException;
import com.fiap.autoescola.model.Aluno;
import com.fiap.autoescola.model.Instrucao;
import com.fiap.autoescola.model.Instrutor;
import com.fiap.autoescola.model.StatusInstrucao;
import com.fiap.autoescola.repository.InstrucaoRepository;
import com.fiap.autoescola.repository.InstrutorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrucaoService {
    private final InstrucaoRepository instrucaoRepository;
    private final InstrutorRepository instrutorRepository;
    private final AlunoService alunoService;
    private final InstrutorService instrutorService;
    private final AgendamentoProperties properties;
    private final Clock clock;

    @Transactional
    public InstrucaoResponse agendar(AgendarInstrucaoRequest request) {
        Aluno aluno = alunoService.buscarEntidade(request.alunoId());
        if (!aluno.isAtivo()) {
            throw new BusinessRuleException("Não é possível agendar instruções para um aluno inativo.");
        }

        validarHorarioDeFuncionamento(request.dataHora());
        validarAntecedenciaMinima(request.dataHora());
        validarLimiteDiarioDoAluno(aluno.getId(), request.dataHora());

        Instrutor instrutor = resolverInstrutor(request.instrutorId(), request.dataHora());

        Instrucao instrucao = Instrucao.builder()
                .aluno(aluno)
                .instrutor(instrutor)
                .dataHora(request.dataHora())
                .status(StatusInstrucao.AGENDADA)
                .build();

        Instrucao salva = instrucaoRepository.save(instrucao);
        log.info("Instrução agendada: id={} aluno={} instrutor={} dataHora={}",
                salva.getId(), aluno.getId(), instrutor.getId(), request.dataHora());
        return InstrucaoResponse.from(salva);
    }

    @Transactional
    public InstrucaoResponse cancelar(Long id, CancelarInstrucaoRequest request) {
        Instrucao instrucao = instrucaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instrução não encontrada: id=" + id));

        if (instrucao.getStatus() == StatusInstrucao.CANCELADA) {
            throw new BusinessRuleException("Esta instrução já está cancelada.");
        }

        LocalDateTime limiteMinimo = LocalDateTime.now(clock)
                .plusHours(properties.cancelamento().antecedenciaMinimaHoras());
        if (instrucao.getDataHora().isBefore(limiteMinimo)) {
            throw new BusinessRuleException(
                    "O cancelamento só pode ser feito com antecedência mínima de "
                            + properties.cancelamento().antecedenciaMinimaHoras() + " horas.");
        }

        instrucao.setStatus(StatusInstrucao.CANCELADA);
        instrucao.setMotivoCancelamento(request.motivo());
        instrucao.setCanceladoEm(Instant.now());

        Instrucao salva = instrucaoRepository.save(instrucao);
        log.info("Instrução cancelada: id={} motivo={}", id, request.motivo());
        return InstrucaoResponse.from(salva);
    }

    @Transactional(readOnly = true)
    public InstrucaoResponse buscarPorId(Long id) {
        Instrucao instrucao = instrucaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instrução não encontrada: id=" + id));
        return InstrucaoResponse.from(instrucao);
    }

    private void validarHorarioDeFuncionamento(LocalDateTime dataHora) {
        DayOfWeek dia = dataHora.getDayOfWeek();
        if (dia == DayOfWeek.SUNDAY) {
            throw new BusinessRuleException("A auto-escola não funciona aos domingos.");
        }

        var agendaCfg = properties.agendamento();
        LocalTime horaInicio = dataHora.toLocalTime();
        LocalTime ultimoInicioPermitido = agendaCfg.fechamento().minusMinutes(agendaCfg.duracaoMinutos());

        if (horaInicio.isBefore(agendaCfg.abertura()) || horaInicio.isAfter(ultimoInicioPermitido)) {
            throw new BusinessRuleException(String.format(
                    "Horário fora do funcionamento. As instruções (duração de %d min) devem iniciar entre %s e %s, de segunda a sábado.",
                    agendaCfg.duracaoMinutos(), agendaCfg.abertura(), ultimoInicioPermitido));
        }
    }

    private void validarAntecedenciaMinima(LocalDateTime dataHora) {
        int minutos = properties.agendamento().antecedenciaMinimaMinutos();
        LocalDateTime limiteMinimo = LocalDateTime.now(clock).plusMinutes(minutos);
        if (dataHora.isBefore(limiteMinimo)) {
            throw new BusinessRuleException(
                    "As instruções devem ser agendadas com antecedência mínima de " + minutos + " minutos.");
        }
    }

    private void validarLimiteDiarioDoAluno(Long alunoId, LocalDateTime dataHora) {
        LocalDateTime inicioDoDia = dataHora.toLocalDate().atStartOfDay();
        LocalDateTime fimDoDia = inicioDoDia.plusDays(1);
        long quantidade = instrucaoRepository.countNaoCanceladasDoAlunoNoDia(alunoId, inicioDoDia, fimDoDia);

        int limite = properties.agendamento().maxInstrucoesPorAlunoDia();
        if (quantidade >= limite) {
            throw new BusinessRuleException(
                    "O aluno já possui o limite de " + limite + " instruções agendadas para este dia.");
        }
    }

    private Instrutor resolverInstrutor(Long instrutorId, LocalDateTime dataHora) {
        if (instrutorId != null) {
            Instrutor instrutor = instrutorService.buscarEntidade(instrutorId);
            if (!instrutor.isAtivo()) {
                throw new BusinessRuleException("Não é possível agendar instruções com um instrutor inativo.");
            }
            boolean conflito = instrucaoRepository.existsByInstrutorIdAndDataHoraAndStatusNot(
                    instrutorId, dataHora, StatusInstrucao.CANCELADA);
            if (conflito) {
                throw new BusinessRuleException("O instrutor já possui uma instrução agendada neste horário.");
            }
            return instrutor;
        }

        List<Instrutor> disponiveis = instrutorRepository.findDisponiveisEm(dataHora);
        if (disponiveis.isEmpty()) {
            throw new BusinessRuleException("Nenhum instrutor disponível neste horário.");
        }
        return disponiveis.get(ThreadLocalRandom.current().nextInt(disponiveis.size()));
    }
}
