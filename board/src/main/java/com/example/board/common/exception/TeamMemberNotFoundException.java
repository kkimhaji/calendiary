package com.example.board.common.exception;

public class TeamMemberNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE = "team member not found";

    public TeamMemberNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public TeamMemberNotFoundException(String message) {
        super(message);
    }

    public static TeamMemberNotFoundException defaultException() {
        return new TeamMemberNotFoundException();
    }

    public static TeamMemberNotFoundException withMessage(String message) {
        return new TeamMemberNotFoundException(message);
    }
}

