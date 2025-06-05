package com.example.board.common.exception;

public record ErrorResponse(
        String code,
        String message
) {
}
