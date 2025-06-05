package com.example.board.member.dto;

public record VerifyUserDTO(
        String email,
        String verificationCode
) {
}
