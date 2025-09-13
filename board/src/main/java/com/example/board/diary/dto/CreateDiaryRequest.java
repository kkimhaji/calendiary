package com.example.board.diary.dto;

import com.example.board.diary.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateDiaryRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Visibility visibility,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate diaryDate
) {
    public CreateDiaryRequest {
        if (diaryDate == null) {
            diaryDate = LocalDate.now();
        }
    }
}