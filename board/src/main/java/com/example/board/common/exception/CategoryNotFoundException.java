package com.example.board.common.exception;

public class CategoryNotFoundException extends ResourceNotFoundException{
    private static final String DEFAULT_MESSAGE = "category not found";

    public CategoryNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public static CategoryNotFoundException defaultException() {
        return new CategoryNotFoundException();
    }

    public static CategoryNotFoundException withMessage(String message) {
        return new CategoryNotFoundException(message);
    }
}
