package com.example.kafka.exception;

public class NoSuchPersonException extends RuntimeException {
    public NoSuchPersonException(Long id) {
        super("Person with id " + id + " does not exist");
    }
}
