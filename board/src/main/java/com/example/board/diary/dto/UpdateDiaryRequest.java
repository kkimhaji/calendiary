package com.example.board.diary.dto;

import com.example.board.diary.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record UpdateDiaryRequest(
        String title,
        String content,
        Visibility visibility,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate diaryDate,
        List<Long> deleteImageIds
) {
    public UpdateDiaryRequest {
        if (deleteImageIds == null) {
            deleteImageIds = new ArrayList<>();
        }
    }
}