package com.rh.customers.application.exception.db;

public class DuplicateResourceException extends DatabaseException{
    public DuplicateResourceException(String resourceName, String id) {
        super("Resource '" + resourceName + "' with ID '" + id + "' already exists.");
    }
}
