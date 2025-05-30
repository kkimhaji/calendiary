package com.example.board.dto.post;

import com.example.board.domain.post.Post;
import com.example.board.domain.category.TeamCategory;
import org.springframework.web.multipart.MultipartFile;

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
