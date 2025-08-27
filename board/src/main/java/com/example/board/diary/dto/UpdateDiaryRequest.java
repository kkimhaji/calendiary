package com.example.board.diary.dto;

import com.example.board.diary.Visibility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record UpdateDiaryRequest(
        String title,
        String content,
        LocalDateTime createdDate,
        Visibility visibility,
        List<Long> deleteImageIds
) {
}
