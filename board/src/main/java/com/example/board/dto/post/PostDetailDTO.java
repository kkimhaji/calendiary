package com.example.board.dto.post;

import com.example.board.domain.post.Post;

import java.time.LocalDateTime;

public record PostDetailDTO(
        Long id,
        String title,
        String content,
        AuthorDTO author,
        String categoryName,
        LocalDateTime createdAt
) {
    public static PostDetailDTO from(Post post){
        return new PostDetailDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                AuthorDTO.from(post.getAuthor()),
                post.getCategory().getName(),
                post.getCreatedDate()
        );
    }
}
