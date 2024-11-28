package com.example.board.exception;

public class RoleDeletionException extends RuntimeException{
    public RoleDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoleDeletionException(String message) {
        super(message);
    }
}
