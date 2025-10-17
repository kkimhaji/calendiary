package com.example.board.common.exception;

public class VerificationCodeExpiredException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "인증 코드가 만료되었습니다. 새로운 인증 코드를 요청해주세요.";

    public VerificationCodeExpiredException() {
        super(DEFAULT_MESSAGE);
    }

    public VerificationCodeExpiredException(String message) {
        super(message);
    }

    public VerificationCodeExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
