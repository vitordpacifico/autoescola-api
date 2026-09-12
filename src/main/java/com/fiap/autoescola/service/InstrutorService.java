package com.fiap.autoescola.service;

import com.fiap.autoescola.dto.instrutor.AtualizarInstrutorRequest;
import com.fiap.autoescola.dto.instrutor.CriarInstrutorRequest;
import com.fiap.autoescola.dto.instrutor.InstrutorDetalheResponse;
import com.fiap.autoescola.dto.instrutor.InstrutorResponse;
import com.fiap.autoescola.exception.DuplicateResourceException;
import com.fiap.autoescola.exception.ResourceNotFoundException;
import com.fiap.autoescola.model.Instrutor;
import com.fiap.autoescola.repository.InstrutorRepository;
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
public class InstrutorService {
    private final InstrutorRepository instrutorRepository;

    @Transactional
    public InstrutorDetalheResponse cadastrar(CriarInstrutorRequest request) {
        if (instrutorRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Já existe um instrutor com este e-mail.");
        }
        if (instrutorRepository.existsByCnh(request.cnh())) {
            throw new DuplicateResourceException("Já existe um instrutor com esta CNH.");
        }

        Instrutor instrutor = Instrutor.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .cnh(request.cnh())
                .especialidade(request.especialidade())
                .endereco(EnderecoMapper.toEntity(request.endereco()))
                .ativo(true)
                .build();

        Instrutor salvo = instrutorRepository.save(instrutor);
        log.info("Instrutor cadastrado: id={} nome='{}'", salvo.getId(), salvo.getNome());
        return InstrutorDetalheResponse.from(salvo);
    }

    @Transactional(readOnly = true)
    public Page<InstrutorResponse> listar(Pageable pageable) {
        return instrutorRepository.findAllByOrderByNomeAsc(pageable).map(InstrutorResponse::from);
    }

    @Transactional(readOnly = true)
    public InstrutorDetalheResponse buscarPorId(Long id) {
        return InstrutorDetalheResponse.from(buscarEntidade(id));
    }

    @Transactional
    public InstrutorDetalheResponse atualizar(Long id, AtualizarInstrutorRequest request) {
        Instrutor instrutor = buscarEntidade(id);
        instrutor.setNome(request.nome());
        instrutor.setTelefone(request.telefone());
        EnderecoMapper.copyInto(request.endereco(), instrutor.getEndereco());

        Instrutor salvo = instrutorRepository.save(instrutor);
        log.info("Instrutor atualizado: id={}", salvo.getId());
        return InstrutorDetalheResponse.from(salvo);
    }

    @Transactional
    public void excluir(Long id) {
        Instrutor instrutor = buscarEntidade(id);
        instrutor.setAtivo(false);
        instrutorRepository.save(instrutor);
        log.info("Instrutor inativado (exclusão lógica): id={}", id);
    }

    Instrutor buscarEntidade(Long id) {
        return instrutorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instrutor não encontrado: id=" + id));
    }
}
