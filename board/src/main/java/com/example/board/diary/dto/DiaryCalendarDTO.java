package com.example.board.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryCalendarDTO(
        Long diaryId,
        String title,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate diaryDate,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdDate,
        String thumbnailImageUrl,
        Long imageCount
) {
    public DiaryCalendarDTO(Long diaryId, String title, LocalDateTime createdDate,
                            String thumbnailImageUrl, Long imageCount) {
        this(diaryId,
                title,
                createdDate != null ? createdDate.toLocalDate() : null,
                createdDate,
                thumbnailImageUrl,
                imageCount != null ? imageCount : 0L);
    }

    public LocalDate getEffectiveDate() {
        return diaryDate != null ? diaryDate :
                (createdDate != null ? createdDate.toLocalDate() : null);
    }
}
