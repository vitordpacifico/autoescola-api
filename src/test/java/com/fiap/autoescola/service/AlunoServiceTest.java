package com.fiap.autoescola.service;

import com.fiap.autoescola.dto.aluno.AlunoDetalheResponse;
import com.fiap.autoescola.dto.aluno.AlunoResponse;
import com.fiap.autoescola.dto.aluno.AtualizarAlunoRequest;
import com.fiap.autoescola.dto.aluno.CriarAlunoRequest;
import com.fiap.autoescola.dto.common.EnderecoDto;
import com.fiap.autoescola.exception.BusinessRuleException;
import com.fiap.autoescola.exception.DuplicateResourceException;
import com.fiap.autoescola.exception.ResourceNotFoundException;
import com.fiap.autoescola.model.Aluno;
import com.fiap.autoescola.model.Endereco;
import com.fiap.autoescola.repository.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {
    private static final String CPF_VALIDO = "52998224725";

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private AlunoService alunoService;

    private EnderecoDto enderecoDto;

    @BeforeEach
    void setUp() {
        enderecoDto = new EnderecoDto("Av. Paulista", "1000", null, "Bela Vista", "São Paulo", "SP", "01310-100");
    }

    private Aluno alunoExistente() {
        return Aluno.builder()
                .id(1L).nome("Maria Oliveira").email("maria@escola.com").telefone("11999999999").cpf(CPF_VALIDO)
                .endereco(new Endereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01000-000"))
                .ativo(true).build();
    }

    @Test
    void cadastrar_comDadosValidos_salvaAlunoAtivo() {
        var request = new CriarAlunoRequest("Maria Oliveira", "maria@escola.com", "11999999999", CPF_VALIDO, enderecoDto);
        when(alunoRepository.existsByEmail("maria@escola.com")).thenReturn(false);
        when(alunoRepository.existsByCpf(CPF_VALIDO)).thenReturn(false);
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(inv -> {
            Aluno a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AlunoDetalheResponse response = alunoService.cadastrar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Maria Oliveira");
        assertThat(response.ativo()).isTrue();
        assertThat(response.endereco().cep()).isEqualTo("01310-100");
    }

    @Test
    void cadastrar_comCpfInvalido_lancaBusinessRuleException() {
        var request = new CriarAlunoRequest("Maria Oliveira", "maria@escola.com", "11999999999", "11111111111", enderecoDto);

        assertThatThrownBy(() -> alunoService.cadastrar(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CPF inválido");
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void cadastrar_comEmailDuplicado_lancaDuplicateResourceException() {
        var request = new CriarAlunoRequest("Maria Oliveira", "maria@escola.com", "11999999999", CPF_VALIDO, enderecoDto);
        when(alunoRepository.existsByEmail("maria@escola.com")).thenReturn(true);

        assertThatThrownBy(() -> alunoService.cadastrar(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("e-mail");
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void cadastrar_comCpfDuplicado_lancaDuplicateResourceException() {
        var request = new CriarAlunoRequest("Maria Oliveira", "maria@escola.com", "11999999999", CPF_VALIDO, enderecoDto);
        when(alunoRepository.existsByEmail("maria@escola.com")).thenReturn(false);
        when(alunoRepository.existsByCpf(CPF_VALIDO)).thenReturn(true);

        assertThatThrownBy(() -> alunoService.cadastrar(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CPF");
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void listar_retornaPaginaMapeadaParaResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        when(alunoRepository.findAllByOrderByNomeAsc(pageable))
                .thenReturn(new PageImpl<>(List.of(alunoExistente()), pageable, 1));

        Page<AlunoResponse> pagina = alunoService.listar(pageable);

        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent()).hasSize(1);
    }

    @Test
    void buscarPorId_inexistente_lancaResourceNotFoundException() {
        when(alunoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alunoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void atualizar_alteraNomeTelefoneEEndereco_masPreservaEmailECpf() {
        Aluno existente = alunoExistente();
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new AtualizarAlunoRequest("Maria O. Souza", "11888888888", enderecoDto);
        AlunoDetalheResponse response = alunoService.atualizar(1L, request);

        assertThat(response.nome()).isEqualTo("Maria O. Souza");
        assertThat(response.telefone()).isEqualTo("11888888888");
        assertThat(response.endereco().logradouro()).isEqualTo("Av. Paulista");
        assertThat(response.email()).isEqualTo("maria@escola.com");
        assertThat(response.cpf()).isEqualTo(CPF_VALIDO);
    }

    @Test
    void excluir_fazExclusaoLogica_semRemoverDoBanco() {
        Aluno existente = alunoExistente();
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(existente));

        alunoService.excluir(1L);

        ArgumentCaptor<Aluno> captor = ArgumentCaptor.forClass(Aluno.class);
        verify(alunoRepository).save(captor.capture());
        assertThat(captor.getValue().isAtivo()).isFalse();
        verify(alunoRepository, never()).delete(any());
        verify(alunoRepository, never()).deleteById(any());
    }
}
