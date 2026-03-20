package com.rh.customers.exception.domain;

public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

}
