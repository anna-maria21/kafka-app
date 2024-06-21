package com.example.kafka.exception;

public class NoSuchOperationException extends RuntimeException {
    public NoSuchOperationException(Long id) {
        super("No such operation: " + id);
    }
}
