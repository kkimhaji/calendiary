package com.example.board.dto.post;

import java.time.LocalDateTime;

public record PostSummaryDTO(
        Long id,
        String title,
        LocalDateTime createdDate
) {
}
