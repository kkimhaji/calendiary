package com.example.board.common.exception;

public class TeamNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE = "team not found";

    public TeamNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public TeamNotFoundException(String message) {
        super(message);
    }

    public static TeamNotFoundException defaultException() {
        return new TeamNotFoundException();
    }

    public static TeamNotFoundException withMessage(String message) {
        return new TeamNotFoundException(message);
    }
}
