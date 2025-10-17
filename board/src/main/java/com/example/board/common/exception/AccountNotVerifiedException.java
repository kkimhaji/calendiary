package com.example.board.common.exception;

public class AccountNotVerifiedException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "계정 인증이 완료되지 않았습니다. 이메일로 전송된 인증 코드를 입력해주세요.";

    private final String email;

    public AccountNotVerifiedException(String email) {
        super(DEFAULT_MESSAGE);
        this.email = email;
    }

    public AccountNotVerifiedException(String email, String message) {
        super(message);
        this.email = email;
    }

    public AccountNotVerifiedException(String email, String message, Throwable cause) {
        super(message, cause);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
