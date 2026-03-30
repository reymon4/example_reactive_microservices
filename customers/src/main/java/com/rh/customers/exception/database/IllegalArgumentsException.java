package com.rh.customers.exception.database;

public class IllegalArgumentsException extends DatabaseException{
    public IllegalArgumentsException(String id, String ex) {
        super("Null constraint violation in resource with ID '" + id + "': " + ex);
    }
}
