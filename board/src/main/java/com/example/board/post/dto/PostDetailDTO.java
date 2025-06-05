package com.example.board.post.dto;

import com.example.board.post.Post;
import com.example.board.post.PostImage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PostDetailDTO(
        Long id,
        String title,
        String content,
        AuthorDTO author,
        String categoryName,
        long viewCount,
        LocalDateTime createdDate,
        List<String> imageUrls
) {
    public static PostDetailDTO from(Post post){

        return new PostDetailDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                AuthorDTO.from(post.getTeamMember()),
                post.getCategory().getName(),
                post.getViewCount(),
                post.getCreatedDate(),
                post.getImages().stream()
                                .map(PostImage::getImageUrl)
                                        .collect(Collectors.toList())
        );
    }
}
