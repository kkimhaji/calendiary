package com.example.board.diary.dto;

import com.example.board.diary.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateDiaryRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull LocalDateTime updatedDate,
        @NotNull Visibility visibility
) {
}
