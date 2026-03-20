package com.rh.customers.exception.domain;

public class DuplicateResourceException extends DomainException{

    public DuplicateResourceException(String id) {
        super("Resource with ID " + id + " already exists.");
    }
}
