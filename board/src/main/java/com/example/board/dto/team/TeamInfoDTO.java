package com.example.board.dto.team;

import java.time.LocalDateTime;

public record TeamInfoDTO(
        Long id,
        String name,
        String description,
        String createdBy,
        LocalDateTime createdAt,
        int memberCount
) {
}
