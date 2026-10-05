package com.fiap.autoescola.integration.viacep;

import com.fiap.autoescola.config.ViaCepProperties;
import com.fiap.autoescola.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ViaCepClientTest {
    private static final String BASE_URL = "https://viacep.com.br/ws";

    private MockRestServiceServer server;
    private ViaCepClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ViaCepClient(builder, new ViaCepProperties(BASE_URL));
    }

    @Test
    void buscar_cepExistente_retornaEnderecoMapeado() {
        String json = """
                {
                  "cep": "01310-100",
                  "logradouro": "Avenida Paulista",
                  "complemento": "de 612 a 1510 - lado par",
                  "bairro": "Bela Vista",
                  "localidade": "São Paulo",
                  "uf": "SP",
                  "ibge": "3550308",
                  "ddd": "11"
                }
                """;
        server.expect(requestTo(BASE_URL + "/01310100/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<ViaCepResponse> resultado = client.buscar("01310100");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().logradouro()).isEqualTo("Avenida Paulista");
        assertThat(resultado.get().localidade()).isEqualTo("São Paulo");
        assertThat(resultado.get().uf()).isEqualTo("SP");
        server.verify();
    }

    @Test
    void buscar_cepInexistente_retornaVazio() {
        server.expect(requestTo(BASE_URL + "/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": \"true\"}", MediaType.APPLICATION_JSON));

        assertThat(client.buscar("99999999")).isEmpty();
    }

    @Test
    void buscar_cepInexistenteNoFormatoAntigoDaApi_retornaVazio() {
        server.expect(requestTo(BASE_URL + "/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": true}", MediaType.APPLICATION_JSON));

        assertThat(client.buscar("99999999")).isEmpty();
    }

    @Test
    void buscar_quandoViaCepFalha_lancaExternalServiceException() {
        server.expect(requestTo(BASE_URL + "/01310100/json/"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.buscar("01310100"))
                .isInstanceOf(ExternalServiceException.class);
    }
}
