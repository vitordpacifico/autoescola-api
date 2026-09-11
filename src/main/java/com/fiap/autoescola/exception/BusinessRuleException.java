package com.fiap.autoescola.exception;

/** Violacao de uma regra de negocio (ex.: horario de funcionamento, antecedencia minima). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
