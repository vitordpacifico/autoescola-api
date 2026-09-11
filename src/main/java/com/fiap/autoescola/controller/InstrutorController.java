package com.fiap.autoescola.controller;

import com.fiap.autoescola.dto.instrutor.AtualizarInstrutorRequest;
import com.fiap.autoescola.dto.instrutor.CriarInstrutorRequest;
import com.fiap.autoescola.dto.instrutor.InstrutorDetalheResponse;
import com.fiap.autoescola.dto.instrutor.InstrutorResponse;
import com.fiap.autoescola.service.InstrutorService;
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
@RequestMapping("/api/v1/instrutores")
@RequiredArgsConstructor
@Tag(name = "Instrutores", description = "CRUD de instrutores (exclusão é lógica)")
public class InstrutorController {

    private static final int TAMANHO_PAGINA_PADRAO = 10;

    private final InstrutorService instrutorService;

    @PostMapping
    public ResponseEntity<InstrutorDetalheResponse> cadastrar(@Valid @RequestBody CriarInstrutorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instrutorService.cadastrar(request));
    }

    /** Paginado (10/página por padrão) e ordenado por nome, conforme o enunciado. */
    @GetMapping
    public ResponseEntity<Page<InstrutorResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + TAMANHO_PAGINA_PADRAO) int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("nome").ascending());
        return ResponseEntity.ok(instrutorService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstrutorDetalheResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(instrutorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstrutorDetalheResponse> atualizar(@PathVariable Long id,
                                                               @Valid @RequestBody AtualizarInstrutorRequest request) {
        return ResponseEntity.ok(instrutorService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        instrutorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
