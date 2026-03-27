package com.rh.customers.exception;
import com.rh.customers.core.GenericResponse;

import com.rh.customers.exception.domain.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<GenericResponse<String>>> handleNotFound(
            NotFoundException ex, ServerHttpRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex, request);
    }


    @ExceptionHandler(DataAccessException.class)
    public Mono<ResponseEntity<GenericResponse<String>>> handleDataAccessException(
            DataAccessException ex, ServerHttpRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,ex, request);
    }



    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<GenericResponse<String>>> handleGenericException(
            Exception ex, ServerHttpRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }

    private Mono<ResponseEntity<GenericResponse<String>>> buildErrorResponse(
            HttpStatus status, Exception ex, ServerHttpRequest request) {
        GenericResponse<String> response = new GenericResponse<>(
                false,
                status.value(),
                ex.getMessage(),
                request.getPath().value(),
                null
        );

        return Mono.just(ResponseEntity.status(status).body(response));
    }
}
