package com.example.board.dto.member;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
        @NotBlank(message = "이메일은 필수 입력값입니다")
        String newPassword
) {
}
