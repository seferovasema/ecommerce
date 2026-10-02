package com.sema.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> validationErrors,
        LocalDateTime timestamp
) {

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                null,
                LocalDateTime.now()
        );
    }

    public static ErrorResponse ofValidation(
            HttpStatus status,
            String message,
            Map<String, String> validationErrors) {

        return new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                validationErrors,
                LocalDateTime.now()
        );
    }
}

