package com.example.board.dto.post;

import com.example.board.domain.post.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String authorName,
        Long categoryId,
        String categoryName,
        int viewCount,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate
) {
    public static PostResponse from(Post post){
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor() != null ? post.getAuthor().getNickname() : "Unknown",
                post.getCategory() != null ? post.getCategory().getId() : null,
                post.getCategory() != null ? post.getCategory().getName() : "No Category",
                post.getViewCount(),
                post.getCreatedDate(),
                post.getModifiedDate()
        );
    }
}
