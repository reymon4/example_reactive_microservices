package com.rh.bank.application.exception.domain;


public class NotFoundException extends DomainException {
    public NotFoundException(String id) {
        super("Resource with ID " + id + " not found.");
    }
}
