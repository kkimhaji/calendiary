package com.example.board.post.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreatePostRequest(
        @NotBlank(message = "제목은 필수입니다")
        String title,
        @NotBlank(message = "내용은 필수입니다")
        String content
) {
}