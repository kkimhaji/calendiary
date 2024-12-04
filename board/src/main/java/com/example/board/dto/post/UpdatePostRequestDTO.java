package com.example.board.dto.post;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record UpdatePostRequestDTO(
        String title,
        String content,
        List<MultipartFile> images,
        List<Long> deleteImageIds
) {
}
