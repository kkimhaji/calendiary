package com.example.board.diary.dto;


import com.example.board.diary.Diary;

import java.time.LocalDateTime;

public record DiaryListDTO(
        Long id,
        String title,
        String content,
        LocalDateTime createdDate,
        String visibility
) {
    public static DiaryListDTO from(Diary diary) {
        return new DiaryListDTO(
                diary.getId(),
                diary.getTitle(),
                diary.getContent(),
                diary.getCreatedDate(),
                diary.getVisibility().name()
        );
    }
}

