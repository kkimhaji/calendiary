package com.example.board.diary.dto;

import com.example.board.diary.Visibility;

import java.util.List;

public record UpdateDiaryRequest(
        String title,
        String content,
        Visibility visibility,
        List<Long> deleteImageIds
) {
}