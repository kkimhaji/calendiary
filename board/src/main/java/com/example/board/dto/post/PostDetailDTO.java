package com.example.board.dto.post;

import com.example.board.domain.post.Comment;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostImage;
import com.example.board.dto.comment.CommentResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.board.dto.comment.CommentResponse.convertToResponse;

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
                AuthorDTO.from(post.getAuthor(), post.getTeamMember()),
                post.getCategory().getName(),
                post.getViewCount(),
                post.getCreatedDate(),
                post.getImages().stream()
                                .map(PostImage::getImageUrl)
                                        .collect(Collectors.toList())
        );
    }
}
