package com.example.board.auth.token.dto;


public record TokenRequestDTO(
        String accessToken,
        String refreshToken
) {
}