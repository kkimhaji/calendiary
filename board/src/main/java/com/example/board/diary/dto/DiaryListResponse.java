package com.example.board.diary.dto;

import java.time.LocalDateTime;

public record DiaryListResponse(
        Long diaryId,
        String title,
        LocalDateTime createdDate,
        String thumbnailImageUrl
) {
}