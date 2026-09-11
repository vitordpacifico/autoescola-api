package com.fiap.autoescola.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.autoescola.dto.aluno.AtualizarAlunoRequest;
import com.fiap.autoescola.dto.aluno.CriarAlunoRequest;
import com.fiap.autoescola.dto.common.EnderecoDto;
import com.fiap.autoescola.dto.instrutor.AtualizarInstrutorRequest;
import com.fiap.autoescola.dto.instrutor.CriarInstrutorRequest;
import com.fiap.autoescola.model.Especialidade;
import com.fiap.autoescola.model.Perfil;
import com.fiap.autoescola.model.Usuario;
import com.fiap.autoescola.repository.AlunoRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InstrutorAlunoIntegrationTest {

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
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    private String token;

    private static final EnderecoDto ENDERECO = new EnderecoDto(
            "Rua das Flores", "100", null, "Centro", "São Paulo", "SP", "01000-000");

    @BeforeEach
    void setUp() {
        instrutorRepository.deleteAll();
        alunoRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = Usuario.builder()
                .username("operador").senhaHash(passwordEncoder.encode("Operador@123"))
                .perfil(Perfil.USER).ativo(true).build();
        usuarioRepository.save(usuario);
        token = jwtService.gerarToken(usuario.getUsername(), usuario.getPerfil().name());
    }

    @Test
    void cadastrarInstrutor_eDepoisListar_ordenadoENoFormatoResumido() throws Exception {
        cadastrarInstrutor("Bruno Silva", "bruno@escola.com", "11987654321");
        cadastrarInstrutor("Ana Souza", "ana@escola.com", "11987654322");

        mockMvc.perform(get("/api/v1/instrutores").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].nome").value("Ana Souza"))
                .andExpect(jsonPath("$.content[0].telefone").doesNotExist())
                .andExpect(jsonPath("$.content[1].nome").value("Bruno Silva"));
    }

    @Test
    void naoPermiteCadastrarDoisInstrutoresComMesmoEmail() throws Exception {
        cadastrarInstrutor("Bruno Silva", "duplicado@escola.com", "11987654321");

        var request = new CriarInstrutorRequest("Outro Nome", "duplicado@escola.com", "11900000000",
                "98765432100", Especialidade.CARROS, ENDERECO);
        mockMvc.perform(post("/api/v1/instrutores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void atualizarInstrutor_naoAceitaAlterarEmailCnhOuEspecialidade() throws Exception {
        Long id = cadastrarInstrutor("Bruno Silva", "bruno2@escola.com", "11987654321");

        // O DTO de atualizacao nem aceita esses campos - o request so tem nome/telefone/endereco.
        var request = new AtualizarInstrutorRequest("Bruno Silva Atualizado", "11911112222", ENDERECO);
        mockMvc.perform(put("/api/v1/instrutores/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bruno Silva Atualizado"))
                .andExpect(jsonPath("$.email").value("bruno2@escola.com"))
                .andExpect(jsonPath("$.cnh").value("12345678901"));
    }

    @Test
    void excluirInstrutor_naoRemoveDoBanco_apenasInativa() throws Exception {
        Long id = cadastrarInstrutor("Bruno Silva", "bruno3@escola.com", "11987654321");

        mockMvc.perform(delete("/api/v1/instrutores/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        var instrutor = instrutorRepository.findById(id).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(instrutor.isAtivo()).isFalse();
    }

    @Test
    void cadastrarAluno_comCpfInvalido_retorna422() throws Exception {
        var request = new CriarAlunoRequest("João", "joao@escola.com", "11999999999", "11111111111", ENDERECO);
        mockMvc.perform(post("/api/v1/alunos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cadastrarAluno_eListar_noFormatoResumido() throws Exception {
        var request = new CriarAlunoRequest("Maria Oliveira", "maria@escola.com", "11999999999",
                "52998224725", ENDERECO);
        mockMvc.perform(post("/api/v1/alunos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/alunos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Maria Oliveira"))
                .andExpect(jsonPath("$.content[0].cpf").value("52998224725"))
                .andExpect(jsonPath("$.content[0].telefone").doesNotExist());
    }

    private Long cadastrarInstrutor(String nome, String email, String telefone) throws Exception {
        var request = new CriarInstrutorRequest(nome, email, telefone, gerarCnh(), Especialidade.CARROS, ENDERECO);
        String responseJson = mockMvc.perform(post("/api/v1/instrutores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("id").asLong();
    }

    private int contador = 0;

    private String gerarCnh() {
        contador++;
        return String.format("%011d", 12345678900L + contador);
    }
}
