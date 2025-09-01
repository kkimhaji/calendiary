package com.example.board.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public record UpdatePostRequestDTO(
        @JsonProperty("title")
        @NotBlank(message = "제목은 필수입니다")
        String title,
        @JsonProperty("content")
        @NotBlank(message = "내용은 필수입니다")
        String content,
        @JsonProperty("deleteImageIds")
        List<Long> deleteImageIds
) {
        public UpdatePostRequestDTO {
                if (deleteImageIds == null) {
                        deleteImageIds = new ArrayList<>();
                }
        }
}