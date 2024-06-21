package com.example.kafka.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class JsonBalanceControllerAdvice {

    @ExceptionHandler(NoSuchPersonException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNoSuchPersonException(NoSuchPersonException ex) {
        return new ApiError(ex.getMessage());
    }

    @ExceptionHandler(NoSuchAccountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNoSuchAccountException(NoSuchAccountException ex) {
        return new ApiError(ex.getMessage());
    }

    @ExceptionHandler(NoSuchOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNoSuchOperationException(NoSuchOperationException ex) {
        return new ApiError(ex.getMessage());
    }

    @ExceptionHandler(AlreadyConfirmedOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleAlreadyConfirmedOperationException(AlreadyConfirmedOperationException ex) {
        return new ApiError(ex.getMessage());
    }


}
