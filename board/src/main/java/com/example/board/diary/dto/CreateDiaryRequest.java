package com.example.board.diary.dto;

import com.example.board.diary.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDiaryRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Visibility visibility
) {
}