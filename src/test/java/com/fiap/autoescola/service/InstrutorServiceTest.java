package com.fiap.autoescola.service;

import com.fiap.autoescola.dto.common.EnderecoDto;
import com.fiap.autoescola.dto.instrutor.AtualizarInstrutorRequest;
import com.fiap.autoescola.dto.instrutor.CriarInstrutorRequest;
import com.fiap.autoescola.dto.instrutor.InstrutorDetalheResponse;
import com.fiap.autoescola.dto.instrutor.InstrutorResponse;
import com.fiap.autoescola.exception.DuplicateResourceException;
import com.fiap.autoescola.exception.ResourceNotFoundException;
import com.fiap.autoescola.model.Endereco;
import com.fiap.autoescola.model.Especialidade;
import com.fiap.autoescola.model.Instrutor;
import com.fiap.autoescola.repository.InstrutorRepository;
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
class InstrutorServiceTest {
    @Mock
    private InstrutorRepository instrutorRepository;

    @InjectMocks
    private InstrutorService instrutorService;

    private EnderecoDto enderecoDto;

    @BeforeEach
    void setUp() {
        enderecoDto = new EnderecoDto("Av. Paulista", "1000", null, "Bela Vista", "São Paulo", "SP", "01310-100");
    }

    private Instrutor instrutorExistente() {
        return Instrutor.builder()
                .id(1L).nome("Bruno Silva").email("bruno@escola.com").telefone("11988888888")
                .cnh("11122233344").especialidade(Especialidade.CARROS)
                .endereco(new Endereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01000-000"))
                .ativo(true).build();
    }

    private CriarInstrutorRequest criarRequest() {
        return new CriarInstrutorRequest("Bruno Silva", "bruno@escola.com", "11988888888", "11122233344",
                Especialidade.CARROS, enderecoDto);
    }

    @Test
    void cadastrar_comDadosValidos_salvaInstrutorAtivo() {
        when(instrutorRepository.existsByEmail("bruno@escola.com")).thenReturn(false);
        when(instrutorRepository.existsByCnh("11122233344")).thenReturn(false);
        when(instrutorRepository.save(any(Instrutor.class))).thenAnswer(inv -> {
            Instrutor i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        InstrutorDetalheResponse response = instrutorService.cadastrar(criarRequest());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Bruno Silva");
        assertThat(response.especialidade()).isEqualTo(Especialidade.CARROS);
        assertThat(response.ativo()).isTrue();
    }

    @Test
    void cadastrar_comEmailDuplicado_lancaDuplicateResourceException() {
        when(instrutorRepository.existsByEmail("bruno@escola.com")).thenReturn(true);

        assertThatThrownBy(() -> instrutorService.cadastrar(criarRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("e-mail");
        verify(instrutorRepository, never()).save(any());
    }

    @Test
    void cadastrar_comCnhDuplicada_lancaDuplicateResourceException() {
        when(instrutorRepository.existsByEmail("bruno@escola.com")).thenReturn(false);
        when(instrutorRepository.existsByCnh("11122233344")).thenReturn(true);

        assertThatThrownBy(() -> instrutorService.cadastrar(criarRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CNH");
        verify(instrutorRepository, never()).save(any());
    }

    @Test
    void listar_retornaPaginaMapeadaParaResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        when(instrutorRepository.findAllByOrderByNomeAsc(pageable))
                .thenReturn(new PageImpl<>(List.of(instrutorExistente()), pageable, 1));

        Page<InstrutorResponse> pagina = instrutorService.listar(pageable);

        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent()).hasSize(1);
    }

    @Test
    void buscarPorId_inexistente_lancaResourceNotFoundException() {
        when(instrutorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instrutorService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void atualizar_alteraNomeTelefoneEEndereco_masPreservaEmailCnhEEspecialidade() {
        Instrutor existente = instrutorExistente();
        when(instrutorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(instrutorRepository.save(any(Instrutor.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new AtualizarInstrutorRequest("Bruno S. Lima", "11977777777", enderecoDto);
        InstrutorDetalheResponse response = instrutorService.atualizar(1L, request);

        assertThat(response.nome()).isEqualTo("Bruno S. Lima");
        assertThat(response.telefone()).isEqualTo("11977777777");
        assertThat(response.endereco().logradouro()).isEqualTo("Av. Paulista");
        assertThat(response.email()).isEqualTo("bruno@escola.com");
        assertThat(response.cnh()).isEqualTo("11122233344");
        assertThat(response.especialidade()).isEqualTo(Especialidade.CARROS);
    }

    @Test
    void excluir_fazExclusaoLogica_semRemoverDoBanco() {
        Instrutor existente = instrutorExistente();
        when(instrutorRepository.findById(1L)).thenReturn(Optional.of(existente));

        instrutorService.excluir(1L);

        ArgumentCaptor<Instrutor> captor = ArgumentCaptor.forClass(Instrutor.class);
        verify(instrutorRepository).save(captor.capture());
        assertThat(captor.getValue().isAtivo()).isFalse();
        verify(instrutorRepository, never()).delete(any());
        verify(instrutorRepository, never()).deleteById(any());
    }
}
