package com.example.board.member.dto;

import lombok.Builder;

@Builder
public record RegisterRequestDTO(
        String nickname,
        String email,
        String password
) {
}
