package com.example.board.dto.member;

import lombok.Getter;
import lombok.Setter;

public record VerifyUserDTO(
        String email,
        String verificationCode
) {
}
