package com.example.board.dto.post;

import com.example.board.domain.post.Comment;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostImage;
import com.example.board.dto.comment.CommentResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PostDetailDTO(
        Long id,
        String title,
        String content,
        AuthorDTO author,
        String categoryName,
        LocalDateTime createdAt,
        List<String> imageUrls,
        List<CommentResponse> comments
) {
    public static PostDetailDTO from(Post post, List<Comment> comments){
        return new PostDetailDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                AuthorDTO.from(post.getAuthor()),
                post.getCategory().getName(),
                post.getCreatedDate(),
                post.getImages().stream()
                                .map(PostImage::getImageUrl)
                                        .collect(Collectors.toList()),
                post.getComments().stream()
                        .filter(comment -> comment.getParent() == null)
                        .map(CommentResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
