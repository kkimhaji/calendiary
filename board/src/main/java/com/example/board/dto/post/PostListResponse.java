package com.example.board.dto.post;

import com.example.board.domain.post.Post;

import java.time.LocalDateTime;

public record PostListResponse(
        Long id,
        String title,
        String authorName,
        String categoryName,
        LocalDateTime createdDate
) {
    public static PostListResponse from(Post post){
        return new PostListResponse(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getUsername(),
                post.getCategory().getName(),
                post.getCreatedDate()
        );
    }
}
