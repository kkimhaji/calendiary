package com.example.board.common.exception;

public class PostNotFoundException extends ResourceNotFoundException{
    private static final String DEFAULT_MESSAGE = "post not found";

    public PostNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public PostNotFoundException(String message) {
        super(message);
    }

    public static PostNotFoundException defaultException() {
        return new PostNotFoundException();
    }

    public static PostNotFoundException withMessage(String message) {
        return new PostNotFoundException(message);
    }
}
