package com.example.kafka.exception;

public class AlreadyConfirmedOperationException extends RuntimeException {
    public AlreadyConfirmedOperationException() {
        super("Operation already confirmed!");
    }
}
