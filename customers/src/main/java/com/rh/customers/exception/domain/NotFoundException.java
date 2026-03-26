package com.rh.customers.exception.domain;

public class NotFoundException extends DomainException{
    public NotFoundException(String id) {
        super("Resource with ID " + id + " not found.");
    }
}
