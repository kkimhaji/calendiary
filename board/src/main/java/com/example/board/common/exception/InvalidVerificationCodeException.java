package com.example.board.common.exception;

public class InvalidVerificationCodeException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "인증 코드가 일치하지 않습니다. 다시 확인해주세요.";

    public InvalidVerificationCodeException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidVerificationCodeException(String message) {
        super(message);
    }

    public InvalidVerificationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
