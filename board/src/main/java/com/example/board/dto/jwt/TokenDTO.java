package com.example.board.dto.jwt;

public record TokenDTO(
        String grantType,
        String accessToken,
        String refreshToken,
        Long accessTokenExpired
) {
}
