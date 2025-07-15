package com.example.board.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TeamAccessDeniedException extends RuntimeException {
    public TeamAccessDeniedException(String message) {
        super(message);
    }

    public TeamAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}