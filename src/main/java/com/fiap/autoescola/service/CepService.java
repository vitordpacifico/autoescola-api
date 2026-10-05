package com.fiap.autoescola.service;

import com.fiap.autoescola.dto.cep.CepResponse;
import com.fiap.autoescola.exception.ResourceNotFoundException;
import com.fiap.autoescola.integration.viacep.ViaCepClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CepService {
    private final ViaCepClient viaCepClient;

    public CepResponse consultar(String cep) {
        String somenteDigitos = cep.replaceAll("\\D", "");
        return viaCepClient.buscar(somenteDigitos)
                .map(CepResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("CEP não encontrado: " + cep));
    }
}
