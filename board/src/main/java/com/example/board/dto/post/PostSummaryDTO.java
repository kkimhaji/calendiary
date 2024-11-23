package com.example.board.dto.post;

import java.time.LocalDateTime;

public record PostSummaryDTO(
        Long id,
        String title,
        LocalDateTime createdDate
) {
    public PostSummaryDTO(Long id, String title, LocalDateTime createdDate) {
        this.id = id;
        this.title = title;
        this.createdDate = createdDate;
    }
}
