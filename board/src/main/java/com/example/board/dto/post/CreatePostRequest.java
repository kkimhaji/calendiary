package com.example.board.dto.post;

public record CreatePostRequest(
    String title,
    String content,
    Long categoryId) {}
