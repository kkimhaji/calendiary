package com.example.board.diary.dto;

import com.example.board.diary.Diary;
import com.example.board.diary.DiaryImage;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DiaryDetailResponse(
        Long diaryId,
        String title,
        String content,
        String authorNickname,
        LocalDateTime createdDate,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate diaryDate,
        String visibility,
        List<String> imageUrls
) {
    public static DiaryDetailResponse from(Diary d) {
        return new DiaryDetailResponse(
                d.getId(),
                d.getTitle(),
                d.getContent(),
                d.getAuthor().getNickname(),
                d.getCreatedDate(),// ← createdDate 사용
                d.getDiaryDate(),
                d.getVisibility().name(),
                d.getImages().stream()
                        .map(DiaryImage::getImageUrl)
                        .toList()
        );
    }
}