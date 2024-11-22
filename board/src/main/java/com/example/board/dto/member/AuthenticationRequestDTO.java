package com.example.board.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public record AuthenticationRequestDTO(
        String email,
        String password
) {
}
