package com.example.board.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PostValidationException extends RuntimeException {
    public PostValidationException(String message) {
        super(message);
    }

    public PostValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
