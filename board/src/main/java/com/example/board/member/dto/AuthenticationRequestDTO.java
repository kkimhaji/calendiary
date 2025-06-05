package com.example.board.member.dto;

public record AuthenticationRequestDTO(
        String email,
        String password,
        Boolean rememberMe
) {
}
