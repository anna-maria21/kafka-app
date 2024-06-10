package com.example.kafka.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class JsonBalanceControllerAdvice {

    @ExceptionHandler(NoSuchAccountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleJsonParseException(NoSuchAccountException ex) {
        String errorMessage = "Invalid JSON format: " + ex.getMessage();
        return new ApiError(errorMessage);
    }

}
