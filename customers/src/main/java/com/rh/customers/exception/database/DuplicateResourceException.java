package com.rh.customers.exception.database;

public class DuplicateResourceException extends DatabaseException{
    public DuplicateResourceException(String resourceName, String id) {
        super("Resource '" + resourceName + "' with ID '" + id + "' already exists.");
    }
}
