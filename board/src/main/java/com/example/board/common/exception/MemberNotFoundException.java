package com.example.board.common.exception;

public class MemberNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE = "member not found";

    public MemberNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public MemberNotFoundException(String message) {
        super(message);
    }

    public static MemberNotFoundException defaultException() {
        return new MemberNotFoundException();
    }

    public static MemberNotFoundException withMessage(String message) {
        return new MemberNotFoundException(message);
    }
}
