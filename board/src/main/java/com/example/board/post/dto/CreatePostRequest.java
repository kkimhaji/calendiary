package com.example.board.post.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreatePostRequest(
        String title,
        String content,
        List<MultipartFile> images
) {
}