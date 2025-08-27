package com.example.board.common.exception;

public class DiaryNotFoundException extends ResourceNotFoundException {
    private static final String DEFAULT_MESSAGE = "diary not found";

    public DiaryNotFoundException(){
        super(DEFAULT_MESSAGE);
    }

    public DiaryNotFoundException(String message) {
        super(message);
    }
}
