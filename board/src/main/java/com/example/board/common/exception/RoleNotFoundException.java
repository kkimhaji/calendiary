package com.example.board.common.exception;

public class RoleNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE = "role not found";

    public RoleNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public RoleNotFoundException(String message) {
        super(message);
    }

    public static RoleNotFoundException defaultException() {
        return new RoleNotFoundException();
    }

    public static RoleNotFoundException withMessage(String message) {
        return new RoleNotFoundException(message);
    }
}