package com.example.board.diary.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryCalendarDTO(
        Long diaryId,
        LocalDate date,
        String thumbnailImageUrl,
        long imageCount
) {
}
