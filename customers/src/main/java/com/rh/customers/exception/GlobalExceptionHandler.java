package com.rh.customers.exception;

import com.rh.customers.core.GenericResponse;
import com.rh.customers.exception.domain.DomainException;
import com.rh.customers.exception.domain.DuplicateResourceException;
import com.rh.customers.exception.domain.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<GenericResponse> handleNotFound(NotFoundException ex, ServerHttpRequest request) {
        return buildDomainError(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<GenericResponse> handleDuplicateResource(DuplicateResourceException ex, ServerHttpRequest request) {
        return buildDomainError(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<GenericResponse> handleGenericException(Exception ex, ServerHttpRequest request) {
        log.error("Unhandled exception occurred: ", ex);
        return buildExceptionError(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }

    // Helpers
    private Mono<GenericResponse> buildDomainError(
            HttpStatus status, DomainException ex, ServerHttpRequest request) {
       return Mono.just(new GenericResponse(
               false,
               status.value(),
               ex.getMessage(),
               request.getPath().value(),
               null
       ));
    }

    private Mono<GenericResponse> buildExceptionError(
            HttpStatus status,Exception ex, ServerHttpRequest request) {
        return Mono.just(new GenericResponse(
                false,
                status.value(),
                ex.getMessage(),
                request.getPath().value(),
                null
        ));
    }
}