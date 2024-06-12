package com.example.kafka.exception;


public class NoSuchAccountException extends RuntimeException {
    public NoSuchAccountException(Long id) {
        super("Account with id " + id + " does not exist");
    }
}
