package com.example.board.post.dto;

import java.util.List;

public record UpdatePostRequestDTO(
        String title,
        String content,
        List<Long> deleteImageIds
) {
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Post title cannot be empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }
    }
}
