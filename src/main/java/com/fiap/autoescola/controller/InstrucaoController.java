package com.fiap.autoescola.controller;

import com.fiap.autoescola.dto.instrucao.AgendarInstrucaoRequest;
import com.fiap.autoescola.dto.instrucao.CancelarInstrucaoRequest;
import com.fiap.autoescola.dto.instrucao.InstrucaoResponse;
import com.fiap.autoescola.service.InstrucaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instrucoes")
@RequiredArgsConstructor
@Tag(name = "Instruções", description = "Agendamento e cancelamento de instruções")
public class InstrucaoController {
    private final InstrucaoService instrucaoService;

    @PostMapping
    public ResponseEntity<InstrucaoResponse> agendar(@Valid @RequestBody AgendarInstrucaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instrucaoService.agendar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstrucaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(instrucaoService.buscarPorId(id));
    }

    @PostMapping("/{id}/cancelamento")
    public ResponseEntity<InstrucaoResponse> cancelar(@PathVariable Long id,
                                                       @Valid @RequestBody CancelarInstrucaoRequest request) {
        return ResponseEntity.ok(instrucaoService.cancelar(id, request));
    }
}
