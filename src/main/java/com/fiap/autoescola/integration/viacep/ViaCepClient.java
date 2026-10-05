package com.fiap.autoescola.integration.viacep;

import com.fiap.autoescola.config.ViaCepProperties;
import com.fiap.autoescola.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
@Slf4j
public class ViaCepClient {
    private final RestClient restClient;

    public ViaCepClient(RestClient.Builder builder, ViaCepProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    public Optional<ViaCepResponse> buscar(String cep) {
        try {
            ViaCepResponse response = restClient.get()
                    .uri("/{cep}/json/", cep)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ViaCepResponse.class);

            if (response == null || response.naoEncontrado()) {
                return Optional.empty();
            }
            return Optional.of(response);
        } catch (RestClientException ex) {
            log.error("Falha ao consultar o ViaCEP para o CEP {}: {}", cep, ex.getMessage());
            throw new ExternalServiceException("Serviço de consulta de CEP indisponível no momento.", ex);
        }
    }
}
