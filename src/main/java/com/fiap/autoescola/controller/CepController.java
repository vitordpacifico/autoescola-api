package com.fiap.autoescola.controller;

import com.fiap.autoescola.dto.cep.CepResponse;
import com.fiap.autoescola.service.CepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cep")
@RequiredArgsConstructor
@Tag(name = "CEP", description = "Consulta de endereço por CEP, consumindo o web service externo ViaCEP")
public class CepController {
    private final CepService cepService;

    @GetMapping("/{cep}")
    @Operation(summary = "Consultar endereço por CEP",
            description = "Busca o endereço no ViaCEP para pré-preencher o cadastro de alunos e instrutores.")
    @ApiResponse(responseCode = "200", description = "Endereço encontrado")
    @ApiResponse(responseCode = "400", description = "CEP em formato inválido")
    @ApiResponse(responseCode = "404", description = "CEP não encontrado")
    @ApiResponse(responseCode = "502", description = "ViaCEP indisponível")
    public ResponseEntity<CepResponse> consultar(
            @Parameter(description = "CEP com 8 dígitos, com ou sem hífen", example = "01310-100")
            @PathVariable
            @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido (use 00000-000)") String cep) {
        return ResponseEntity.ok(cepService.consultar(cep));
    }
}
