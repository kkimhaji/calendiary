package com.example.board.auth.token.dto;

public record TokenDTO(
        String grantType,
        String accessToken,
        String refreshToken,
        Long accessTokenExpired
) {
}
