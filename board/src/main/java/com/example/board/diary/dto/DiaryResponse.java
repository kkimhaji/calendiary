package com.example.board.diary.dto;

import com.example.board.diary.Diary;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryResponse (
    Long id,
    String title,
    String content,
    String authorName,
    LocalDate diaryDate,              // 일기 날짜
    LocalDateTime createdDate,        // 작성 시각
    String visibility,                // PUBLIC/PRIVATE 여부
    String thumbnailImageUrl          // 썸네일 이미지(있다면)
) {
        public static DiaryResponse from(Diary diary) {

            return new DiaryResponse(
                    diary.getId(),
                    diary.getTitle(),
                    diary.getContent(),
                    diary.getAuthor().getNickname(),
                    diary.getDiaryDate(),
                    diary.getCreatedDate(),
                    diary.getVisibility().name(),
                    diary.getThumbnailImageUrl() // 썸네일 없으면 null
            );
        }
}
