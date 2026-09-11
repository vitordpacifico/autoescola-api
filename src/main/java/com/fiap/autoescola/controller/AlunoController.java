package com.fiap.autoescola.controller;

import com.fiap.autoescola.dto.aluno.AlunoDetalheResponse;
import com.fiap.autoescola.dto.aluno.AlunoResponse;
import com.fiap.autoescola.dto.aluno.AtualizarAlunoRequest;
import com.fiap.autoescola.dto.aluno.CriarAlunoRequest;
import com.fiap.autoescola.service.AlunoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alunos")
@RequiredArgsConstructor
@Tag(name = "Alunos", description = "CRUD de alunos (exclusão é lógica)")
public class AlunoController {

    private static final int TAMANHO_PAGINA_PADRAO = 10;

    private final AlunoService alunoService;

    @PostMapping
    public ResponseEntity<AlunoDetalheResponse> cadastrar(@Valid @RequestBody CriarAlunoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alunoService.cadastrar(request));
    }

    /** Paginado (10/página por padrão) e ordenado por nome, conforme o enunciado. */
    @GetMapping
    public ResponseEntity<Page<AlunoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + TAMANHO_PAGINA_PADRAO) int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("nome").ascending());
        return ResponseEntity.ok(alunoService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlunoDetalheResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(alunoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlunoDetalheResponse> atualizar(@PathVariable Long id,
                                                           @Valid @RequestBody AtualizarAlunoRequest request) {
        return ResponseEntity.ok(alunoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        alunoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
