package com.example.board.image.dto;

public record ImageUploadResponse(
        String url,
        boolean success,
        String message
        ) {
}
