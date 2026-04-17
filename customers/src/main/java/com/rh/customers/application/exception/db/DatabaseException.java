package com.rh.customers.application.exception.db;

import org.springframework.dao.DataIntegrityViolationException;

public abstract class DatabaseException extends DataIntegrityViolationException{
    public DatabaseException(String message) {
        super(message);
    }
}
