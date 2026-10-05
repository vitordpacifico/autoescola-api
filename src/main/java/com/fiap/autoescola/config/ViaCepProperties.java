package com.fiap.autoescola.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "autoescola.viacep")
public record ViaCepProperties(String baseUrl) {
}
