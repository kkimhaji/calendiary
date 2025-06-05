package com.example.board.member.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
        @NotBlank(message = "변경할 비밀번호를 입력해주세요.")
        String newPassword
) {
}
