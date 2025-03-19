package com.example.board.dto.jwt;


public record TokenRequestDTO(
        String accessToken,
        String refreshToken
) {
}