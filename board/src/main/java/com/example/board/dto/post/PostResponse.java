package com.example.board.dto.post;

import com.example.board.domain.post.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String authorName,
        String categoryName,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static PostResponse from(Post post){
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getNickname(),
                post.getCategory().getName(),
                post.getViewCount(),
                post.getCreatedDate(),
                post.getModifiedDate()
        );
    }
}
