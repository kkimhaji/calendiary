package com.example.board.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryCalendarDTO(
        Long diaryId,
        LocalDateTime createdDateTime,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate diaryDate,
        String thumbnailImageUrl,
        long imageCount
) {
    // LocalDate 변환 헬퍼 메서드
    public LocalDate date() {
        return createdDateTime != null ? createdDateTime.toLocalDate() : null;
    }

    // 기존 생성자도 유지 (하위 호환성)
    public DiaryCalendarDTO(Long diaryId, LocalDateTime createdDateTime,
                            String thumbnailImageUrl, Long imageCount) {
        this(diaryId, null, createdDateTime.toLocalDate(), thumbnailImageUrl, imageCount);
    }
}
