package com.example.board.diary.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryCalendarDTO(
        Long diaryId,
        LocalDateTime createdDateTime,
        String thumbnailImageUrl,
        long imageCount
) {
    // LocalDate 변환 헬퍼 메서드
    public LocalDate date() {
        return createdDateTime != null ? createdDateTime.toLocalDate() : null;
    }
}
