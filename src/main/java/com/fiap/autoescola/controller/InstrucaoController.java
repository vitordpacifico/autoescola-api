package com.fiap.autoescola.controller;

import com.fiap.autoescola.dto.instrucao.AgendarInstrucaoRequest;
import com.fiap.autoescola.dto.instrucao.CancelarInstrucaoRequest;
import com.fiap.autoescola.dto.instrucao.InstrucaoResponse;
import com.fiap.autoescola.service.InstrucaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instrucoes")
@RequiredArgsConstructor
@Tag(name = "Instruções", description = "Agendamento e cancelamento de instruções")
public class InstrucaoController {
    private final InstrucaoService instrucaoService;

    @PostMapping
    @Operation(summary = "Agendar instrução")
    public ResponseEntity<InstrucaoResponse> agendar(@Valid @RequestBody AgendarInstrucaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instrucaoService.agendar(request));
    }

    @GetMapping
    @Operation(summary = "Listar instruções (paginado, ordenado por data e hora)")
    public ResponseEntity<Page<InstrucaoResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("dataHora").ascending());
        return ResponseEntity.ok(instrucaoService.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar instrução")
    public ResponseEntity<InstrucaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(instrucaoService.buscarPorId(id));
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(summary = "Cancelar instrução (motivo obrigatório, 24h de antecedência)")
    public ResponseEntity<InstrucaoResponse> cancelar(@PathVariable Long id,
                                                       @Valid @RequestBody CancelarInstrucaoRequest request) {
        return ResponseEntity.ok(instrucaoService.cancelar(id, request));
    }
}
