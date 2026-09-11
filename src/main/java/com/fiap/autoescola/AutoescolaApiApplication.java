package com.fiap.autoescola;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AutoescolaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoescolaApiApplication.class, args);
    }
}
