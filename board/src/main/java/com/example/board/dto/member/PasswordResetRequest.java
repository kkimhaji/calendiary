package com.example.board.dto.member;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank(message="이메일을 입력해주세요")
        String email
) {
}
