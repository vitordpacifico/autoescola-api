package com.fiap.autoescola.service;

import com.fiap.autoescola.config.AgendamentoProperties;
import com.fiap.autoescola.dto.instrucao.AgendarInstrucaoRequest;
import com.fiap.autoescola.dto.instrucao.CancelarInstrucaoRequest;
import com.fiap.autoescola.exception.BusinessRuleException;
import com.fiap.autoescola.model.Aluno;
import com.fiap.autoescola.model.Instrucao;
import com.fiap.autoescola.model.Instrutor;
import com.fiap.autoescola.model.MotivoCancelamento;
import com.fiap.autoescola.model.StatusInstrucao;
import com.fiap.autoescola.repository.InstrucaoRepository;
import com.fiap.autoescola.repository.InstrutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstrucaoServiceTest {

    @Mock
    private InstrucaoRepository instrucaoRepository;
    @Mock
    private InstrutorRepository instrutorRepository;
    @Mock
    private AlunoService alunoService;
    @Mock
    private InstrutorService instrutorService;

    private InstrucaoService instrucaoService;

    private AgendamentoProperties properties;
    private Aluno alunoAtivo;
    private Instrutor instrutorAtivo;

    @BeforeEach
    void setUp() {
        properties = new AgendamentoProperties(
                new AgendamentoProperties.Agendamento(LocalTime.of(6, 0), LocalTime.of(21, 0), 60, 30, 2),
                new AgendamentoProperties.Cancelamento(24)
        );
        instrucaoService = new InstrucaoService(instrucaoRepository, instrutorRepository, alunoService, instrutorService,
                properties, Clock.systemDefaultZone());

        alunoAtivo = Aluno.builder().id(1L).nome("Aluno Teste").ativo(true).build();
        instrutorAtivo = Instrutor.builder().id(10L).nome("Instrutor Teste").ativo(true).build();

        lenient().when(alunoService.buscarEntidade(1L)).thenReturn(alunoAtivo);
        lenient().when(instrutorService.buscarEntidade(10L)).thenReturn(instrutorAtivo);
    }

    /** Proxima data, pelo menos {@code diasMinimos} a frente, garantida em dia util (seg-sab), 10h. */
    private LocalDateTime proximoHorarioValido(int diasMinimos) {
        LocalDate data = LocalDate.now().plusDays(diasMinimos);
        while (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            data = data.plusDays(1);
        }
        return data.atTime(10, 0);
    }

    @Test
    void agendar_comSucesso_instrutorEspecificado() {
        LocalDateTime dataHora = proximoHorarioValido(2);
        when(instrucaoRepository.countNaoCanceladasDoAlunoNoDia(eq(1L), any(), any())).thenReturn(0L);
        when(instrucaoRepository.existsByInstrutorIdAndDataHoraAndStatusNot(10L, dataHora, StatusInstrucao.CANCELADA))
                .thenReturn(false);
        when(instrucaoRepository.save(any(Instrucao.class))).thenAnswer(inv -> {
            Instrucao i = inv.getArgument(0);
            i.setId(100L);
            return i;
        });

        var response = instrucaoService.agendar(new AgendarInstrucaoRequest(1L, 10L, dataHora));

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.instrutorId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(StatusInstrucao.AGENDADA);
    }

    @Test
    void agendar_falha_quandoDomingo() {
        LocalDate proximoDomingo = LocalDate.now();
        while (proximoDomingo.getDayOfWeek() != DayOfWeek.SUNDAY) {
            proximoDomingo = proximoDomingo.plusDays(1);
        }
        LocalDateTime dataHora = proximoDomingo.plusDays(7).atTime(10, 0); // domingo, bem no futuro

        assertThatThrownBy(() -> instrucaoService.agendar(new AgendarInstrucaoRequest(1L, 10L, dataHora)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("domingo");
    }

    @Test
    void agendar_falha_foraDoHorarioFuncionamento_muitoCedo() {
        LocalDateTime dataHora = proximoHorarioValido(2).toLocalDate().atTime(5, 30);

        assertThatThrownBy(() -> instrucaoService.agendar(new AgendarInstrucaoRequest(1L, 10L, dataHora)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Horário fora do funcionamento");
    }

    @Test
    void agendar_falha_foraDoHorarioFuncionamento_naoCabeAntesDoFechamento() {
        // Fechamento 21:00, duracao 60min -> ultimo inicio permitido e 20:00.
        LocalDateTime dataHora = proximoHorarioValido(2).toLocalDate().atTime(20, 30);

        assertThatThrownBy(() -> instrucaoService.agendar(new AgendarInstrucaoRequest(1L, 10L, dataHora)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Horário fora do funcionamento");
    }

    @Test
    void agendar_falha_antecedenciaMinima() {
        // "Agora" fixado numa quarta-feira as 10h (dentro do horario de funcionamento),
        // para isolar a regra de antecedencia minima da regra de horario de funcionamento.
        LocalDate proximaQuarta = LocalDate.now();
        while (proximaQuarta.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
            proximaQuarta = proximaQuarta.plusDays(1);
        }
        ZoneId zone = ZoneId.systemDefault();
        Instant agoraFixo = proximaQuarta.atTime(10, 0).atZone(zone).toInstant();
        Clock clockFixo = Clock.fixed(agoraFixo, zone);
        InstrucaoService service = new InstrucaoService(instrucaoRepository, instrutorRepository, alunoService,
                instrutorService, properties, clockFixo);

        LocalDateTime dataHora = proximaQuarta.atTime(10, 5); // so 5 min a frente do "agora" fixo (minimo exigido: 30 min)

        assertThatThrownBy(() -> service.agendar(new AgendarInstrucaoRequest(1L, 10L, dataHora)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("antecedência mínima");
    }

    @Test
    void agendar_falha_alunoInativo() {
        Aluno inativo = Aluno.builder().id(2L).nome("Inativo").ativo(false).build();
        when(alunoService.buscarEntidade(2L)).thenReturn(inativo);
        LocalDateTime dataHora = proximoHorarioValido(2);

        assertThatThrownBy(() -> instrucaoService.agendar(new AgendarInstrucaoRequest(2L, 10L, dataHora)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("aluno inativo");
    }

    @Test
    void agendar_falha_instrutorInativo() {
        Instrutor inativo = Instrutor.builder().id(11L).nome("Inativo").ativo(false).build();
        when(instrutorService.buscarEntidade(11L)).thenReturn(inativo);
        LocalDateTime dataHora = proximoHorarioValido(2);
        when(instrucaoRepository.countNaoCanceladasDoAlunoNoDia(eq(1L), any(), any())).thenReturn(0L);

        assertThatThrownBy(() -> instrucaoService.agendar(new AgendarInstrucaoRequest(1L, 11L, dataHora)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("instrutor inativo");
    }

    @Test
    void agendar_falha_limiteDiarioDoAluno() {
        LocalDateTime dataHora = proximoHorarioValido(2);
        when(instrucaoRepository.countNaoCanceladasDoAlunoNoDia(eq(1L), any(), any())).thenReturn(2L);

        assertThatThrownBy(() -> instrucaoService.agendar(new AgendarInstrucaoRequest(1L, 10L, dataHora)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("limite de 2 instruções");
    }

    @Test
    void agendar_falha_conflitoDeHorarioComInstrutor() {
        LocalDateTime dataHora = proximoHorarioValido(2);
        when(instrucaoRepository.countNaoCanceladasDoAlunoNoDia(eq(1L), any(), any())).thenReturn(0L);
        when(instrucaoRepository.existsByInstrutorIdAndDataHoraAndStatusNot(10L, dataHora, StatusInstrucao.CANCELADA))
                .thenReturn(true);

        assertThatThrownBy(() -> instrucaoService.agendar(new AgendarInstrucaoRequest(1L, 10L, dataHora)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("já possui uma instrução agendada");

        verify(instrucaoRepository, never()).save(any());
    }

    @Test
    void agendar_semInstrutorEspecificado_escolheDisponivelAutomaticamente() {
        LocalDateTime dataHora = proximoHorarioValido(2);
        when(instrucaoRepository.countNaoCanceladasDoAlunoNoDia(eq(1L), any(), any())).thenReturn(0L);
        when(instrutorRepository.findDisponiveisEm(dataHora)).thenReturn(List.of(instrutorAtivo));
        when(instrucaoRepository.save(any(Instrucao.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = instrucaoService.agendar(new AgendarInstrucaoRequest(1L, null, dataHora));

        assertThat(response.instrutorId()).isEqualTo(10L);
        verify(instrutorService, never()).buscarEntidade(anyLong());
    }

    @Test
    void agendar_semInstrutorEspecificado_falhaSeNenhumDisponivel() {
        LocalDateTime dataHora = proximoHorarioValido(2);
        when(instrucaoRepository.countNaoCanceladasDoAlunoNoDia(eq(1L), any(), any())).thenReturn(0L);
        when(instrutorRepository.findDisponiveisEm(dataHora)).thenReturn(List.of());

        assertThatThrownBy(() -> instrucaoService.agendar(new AgendarInstrucaoRequest(1L, null, dataHora)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Nenhum instrutor disponível");
    }

    @Test
    void cancelar_comSucesso_quandoComAntecedenciaSuficiente() {
        Instrucao instrucao = Instrucao.builder()
                .id(5L).aluno(alunoAtivo).instrutor(instrutorAtivo)
                .dataHora(LocalDateTime.now().plusDays(3))
                .status(StatusInstrucao.AGENDADA)
                .build();
        when(instrucaoRepository.findById(5L)).thenReturn(Optional.of(instrucao));
        when(instrucaoRepository.save(any(Instrucao.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = instrucaoService.cancelar(5L, new CancelarInstrucaoRequest(MotivoCancelamento.ALUNO_DESISTIU));

        assertThat(response.status()).isEqualTo(StatusInstrucao.CANCELADA);
        assertThat(response.motivoCancelamento()).isEqualTo(MotivoCancelamento.ALUNO_DESISTIU);
    }

    @Test
    void cancelar_falha_quandoForaDoPrazoDe24Horas() {
        Instrucao instrucao = Instrucao.builder()
                .id(6L).aluno(alunoAtivo).instrutor(instrutorAtivo)
                .dataHora(LocalDateTime.now().plusHours(2))
                .status(StatusInstrucao.AGENDADA)
                .build();
        when(instrucaoRepository.findById(6L)).thenReturn(Optional.of(instrucao));

        assertThatThrownBy(() -> instrucaoService.cancelar(6L, new CancelarInstrucaoRequest(MotivoCancelamento.OUTROS)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("antecedência mínima de 24 horas");

        verify(instrucaoRepository, times(1)).findById(6L);
        verify(instrucaoRepository, never()).save(any());
    }

    @Test
    void cancelar_falha_quandoJaCancelada() {
        Instrucao instrucao = Instrucao.builder()
                .id(7L).aluno(alunoAtivo).instrutor(instrutorAtivo)
                .dataHora(LocalDateTime.now().plusDays(5))
                .status(StatusInstrucao.CANCELADA)
                .motivoCancelamento(MotivoCancelamento.OUTROS)
                .build();
        when(instrucaoRepository.findById(7L)).thenReturn(Optional.of(instrucao));

        assertThatThrownBy(() -> instrucaoService.cancelar(7L, new CancelarInstrucaoRequest(MotivoCancelamento.OUTROS)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("já está cancelada");
    }
}
