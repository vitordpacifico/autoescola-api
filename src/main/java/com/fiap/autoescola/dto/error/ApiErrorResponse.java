package com.fiap.autoescola.dto.error;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> detalhes
) {
    public static ApiErrorResponse of(HttpStatus status, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path, null);
    }

    public static ApiErrorResponse of(HttpStatus status, String message, String path, List<String> detalhes) {
        return new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path, detalhes);
    }
}
