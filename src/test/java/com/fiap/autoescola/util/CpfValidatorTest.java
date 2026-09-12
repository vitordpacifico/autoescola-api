package com.fiap.autoescola.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidatorTest {
    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "111.444.777-35", "39053344705"})
    void deveAceitarCpfValido(String cpf) {
        assertThat(CpfValidator.isValid(cpf)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11111111111", "00000000000", "12345678900", "123", "abcdefghijk"})
    void deveRejeitarCpfInvalido(String cpf) {
        assertThat(CpfValidator.isValid(cpf)).isFalse();
    }

    @Test
    void deveRejeitarCpfNulo() {
        assertThat(CpfValidator.isValid(null)).isFalse();
    }
}
