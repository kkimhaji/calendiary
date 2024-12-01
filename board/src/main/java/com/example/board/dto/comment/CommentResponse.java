package com.example.board.dto.comment;

import com.example.board.domain.post.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CommentResponse(
        Long id,
        String content,
        String authorName,
        LocalDateTime createdAt,
        boolean isDeleted,
        List<CommentResponse> replies
) {
    public static CommentResponse from(Comment comment){
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getNickname(),
                comment.getCreatedDate(),
                comment.isDeleted(),
                comment.getReplies().stream()
                        .map(CommentResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
