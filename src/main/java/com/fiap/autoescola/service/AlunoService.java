package com.fiap.autoescola.service;

import com.fiap.autoescola.dto.aluno.AlunoDetalheResponse;
import com.fiap.autoescola.dto.aluno.AlunoResponse;
import com.fiap.autoescola.dto.aluno.AtualizarAlunoRequest;
import com.fiap.autoescola.dto.aluno.CriarAlunoRequest;
import com.fiap.autoescola.exception.BusinessRuleException;
import com.fiap.autoescola.exception.DuplicateResourceException;
import com.fiap.autoescola.exception.ResourceNotFoundException;
import com.fiap.autoescola.model.Aluno;
import com.fiap.autoescola.repository.AlunoRepository;
import com.fiap.autoescola.util.CpfValidator;
import com.fiap.autoescola.util.EnderecoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlunoService {

    private final AlunoRepository alunoRepository;

    @Transactional
    public AlunoDetalheResponse cadastrar(CriarAlunoRequest request) {
        if (!CpfValidator.isValid(request.cpf())) {
            throw new BusinessRuleException("CPF inválido.");
        }
        if (alunoRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Já existe um aluno com este e-mail.");
        }
        if (alunoRepository.existsByCpf(request.cpf())) {
            throw new DuplicateResourceException("Já existe um aluno com este CPF.");
        }

        Aluno aluno = Aluno.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .cpf(request.cpf())
                .endereco(EnderecoMapper.toEntity(request.endereco()))
                .ativo(true)
                .build();

        Aluno salvo = alunoRepository.save(aluno);
        log.info("Aluno cadastrado: id={} nome='{}'", salvo.getId(), salvo.getNome());
        return AlunoDetalheResponse.from(salvo);
    }

    /** Listagem paginada (10/página por padrão), ordenada por nome crescente. */
    @Transactional(readOnly = true)
    public Page<AlunoResponse> listar(Pageable pageable) {
        return alunoRepository.findAllByOrderByNomeAsc(pageable).map(AlunoResponse::from);
    }

    @Transactional(readOnly = true)
    public AlunoDetalheResponse buscarPorId(Long id) {
        return AlunoDetalheResponse.from(buscarEntidade(id));
    }

    /** Apenas nome, telefone e endereço são atualizáveis (e-mail/CPF são imutáveis). */
    @Transactional
    public AlunoDetalheResponse atualizar(Long id, AtualizarAlunoRequest request) {
        Aluno aluno = buscarEntidade(id);
        aluno.setNome(request.nome());
        aluno.setTelefone(request.telefone());
        EnderecoMapper.copyInto(request.endereco(), aluno.getEndereco());

        Aluno salvo = alunoRepository.save(aluno);
        log.info("Aluno atualizado: id={}", salvo.getId());
        return AlunoDetalheResponse.from(salvo);
    }

    /** Exclusão lógica: o aluno é marcado como inativo, nunca removido do banco. */
    @Transactional
    public void excluir(Long id) {
        Aluno aluno = buscarEntidade(id);
        aluno.setAtivo(false);
        alunoRepository.save(aluno);
        log.info("Aluno inativado (exclusão lógica): id={}", id);
    }

    Aluno buscarEntidade(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado: id=" + id));
    }
}
