package com.example.board.dto.member;

import lombok.Builder;

@Builder
public record RegisterRequestDTO(
        String nickname,
        String email,
        String password
) {
}
