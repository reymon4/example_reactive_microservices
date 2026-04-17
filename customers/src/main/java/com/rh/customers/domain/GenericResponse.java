package com.rh.customers.domain;

public record GenericResponse<T> (
        Boolean success,
        Integer statusCode, // HTTP status code (e.g., 200, 400, 404)
        String message,
        String path, // Request path (e.g., "/api/customers/123")
        T data
) {
}