package com.fiap.autoescola.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.autoescola.dto.instrucao.AgendarInstrucaoRequest;
import com.fiap.autoescola.dto.instrucao.CancelarInstrucaoRequest;
import com.fiap.autoescola.model.Aluno;
import com.fiap.autoescola.model.Endereco;
import com.fiap.autoescola.model.Especialidade;
import com.fiap.autoescola.model.MotivoCancelamento;
import com.fiap.autoescola.model.Perfil;
import com.fiap.autoescola.model.Usuario;
import com.fiap.autoescola.repository.AlunoRepository;
import com.fiap.autoescola.repository.InstrucaoRepository;
import com.fiap.autoescola.repository.InstrutorRepository;
import com.fiap.autoescola.repository.UsuarioRepository;
import com.fiap.autoescola.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InstrucaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private InstrutorRepository instrutorRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private InstrucaoRepository instrucaoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    private String token;
    private Long alunoId;
    private Long instrutorId;

    @BeforeEach
    void setUp() {
        instrucaoRepository.deleteAll();
        instrutorRepository.deleteAll();
        alunoRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = Usuario.builder()
                .username("operador").senhaHash(passwordEncoder.encode("Operador@123"))
                .perfil(Perfil.USER).ativo(true).build();
        usuarioRepository.save(usuario);
        token = jwtService.gerarToken(usuario.getUsername(), usuario.getPerfil().name());

        Endereco endereco = new Endereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01000-000");

        var aluno = com.fiap.autoescola.model.Aluno.builder()
                .nome("Aluno Instrução").email("aluno.instrucao@escola.com").telefone("11999999999")
                .cpf("52998224725").endereco(endereco).ativo(true).build();
        alunoId = alunoRepository.save(aluno).getId();

        var instrutor = com.fiap.autoescola.model.Instrutor.builder()
                .nome("Instrutor Instrução").email("instrutor.instrucao@escola.com").telefone("11988888888")
                .cnh("11122233344").especialidade(Especialidade.CARROS).endereco(endereco).ativo(true).build();
        instrutorId = instrutorRepository.save(instrutor).getId();
    }

    private LocalDateTime proximoHorarioValido(int diasMinimos) {
        LocalDate data = LocalDate.now().plusDays(diasMinimos);
        while (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            data = data.plusDays(1);
        }
        return data.atTime(10, 0);
    }

    @Test
    void fluxoCompleto_agendarEDepoisCancelarComAntecedencia() throws Exception {
        LocalDateTime dataHora = proximoHorarioValido(3);
        var agendarRequest = new AgendarInstrucaoRequest(alunoId, instrutorId, dataHora);

        String responseJson = mockMvc.perform(post("/api/v1/instrucoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendarRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andReturn().getResponse().getContentAsString();

        Long instrucaoId = objectMapper.readTree(responseJson).get("id").asLong();

        var cancelarRequest = new CancelarInstrucaoRequest(MotivoCancelamento.ALUNO_DESISTIU);
        mockMvc.perform(post("/api/v1/instrucoes/" + instrucaoId + "/cancelamento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelarRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.motivoCancelamento").value("ALUNO_DESISTIU"));
    }

    @Test
    void naoPermiteAgendarDuasInstrucoesComMesmoInstrutorNoMesmoHorario() throws Exception {
        LocalDateTime dataHora = proximoHorarioValido(3);
        var request = new AgendarInstrucaoRequest(alunoId, instrutorId, dataHora);

        mockMvc.perform(post("/api/v1/instrucoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segundo aluno tentando o mesmo instrutor no mesmo horario.
        Endereco endereco = new Endereco("Rua B", "2", null, "Centro", "São Paulo", "SP", "01000-000");
        Aluno outroAluno = alunoRepository.save(com.fiap.autoescola.model.Aluno.builder()
                .nome("Outro Aluno").email("outro@escola.com").telefone("11977777777")
                .cpf("11144477735").endereco(endereco).ativo(true).build());

        var request2 = new AgendarInstrucaoRequest(outroAluno.getId(), instrutorId, dataHora);
        mockMvc.perform(post("/api/v1/instrucoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoPermiteTerceiraInstrucaoDoMesmoAlunoNoMesmoDia() throws Exception {
        LocalDateTime dataHora = proximoHorarioValido(3);

        // Precisamos de outro instrutor para nao esbarrar no conflito de horario do instrutor.
        Endereco endereco = new Endereco("Rua C", "3", null, "Centro", "São Paulo", "SP", "01000-000");
        var segundoInstrutor = instrutorRepository.save(com.fiap.autoescola.model.Instrutor.builder()
                .nome("Segundo Instrutor").email("segundo@escola.com").telefone("11966666666")
                .cnh("22233344455").especialidade(Especialidade.CARROS).endereco(endereco).ativo(true).build());

        agendar(alunoId, instrutorId, dataHora);
        agendar(alunoId, segundoInstrutor.getId(), dataHora.plusHours(2));

        var terceiraRequest = new AgendarInstrucaoRequest(alunoId, null, dataHora.plusHours(4));
        mockMvc.perform(post("/api/v1/instrucoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(terceiraRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoPermiteCancelarComMenosDe24HorasDeAntecedencia() throws Exception {
        // Insere a instrucao diretamente (o teste valida apenas a regra de
        // cancelamento, nao as regras de agendamento).
        var aluno = alunoRepository.findById(alunoId).orElseThrow();
        var instrutor = instrutorRepository.findById(instrutorId).orElseThrow();
        var instrucao = com.fiap.autoescola.model.Instrucao.builder()
                .aluno(aluno).instrutor(instrutor)
                .dataHora(LocalDateTime.now().plusHours(2))
                .status(com.fiap.autoescola.model.StatusInstrucao.AGENDADA)
                .build();
        Long instrucaoId = instrucaoRepository.save(instrucao).getId();

        var cancelarRequest = new CancelarInstrucaoRequest(MotivoCancelamento.OUTROS);
        mockMvc.perform(post("/api/v1/instrucoes/" + instrucaoId + "/cancelamento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelarRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    private Long agendar(Long alunoId, Long instrutorId, LocalDateTime dataHora) throws Exception {
        var request = new AgendarInstrucaoRequest(alunoId, instrutorId, dataHora);
        String responseJson = mockMvc.perform(post("/api/v1/instrucoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("id").asLong();
    }
}
