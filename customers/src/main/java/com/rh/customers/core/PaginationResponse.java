package com.rh.customers.core;

public record PaginationResponse<T>(
        Long totalItems,
        Integer pages,
        T data

) {
}
