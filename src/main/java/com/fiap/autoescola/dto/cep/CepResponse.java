package com.fiap.autoescola.dto.cep;

import com.fiap.autoescola.integration.viacep.ViaCepResponse;

public record CepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String cidade,
        String uf
) {
    public static CepResponse from(ViaCepResponse r) {
        return new CepResponse(r.cep(), r.logradouro(), r.complemento(), r.bairro(), r.localidade(), r.uf());
    }
}
