package com.example.board.team.dto;

import java.time.LocalDateTime;

public record TeamInfoDTO(
        Long id,
        String name,
        String description,
        String created_by,
        LocalDateTime createdAt,
        long memberCount
) {
}
