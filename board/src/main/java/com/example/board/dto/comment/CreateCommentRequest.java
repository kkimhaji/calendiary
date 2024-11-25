package com.example.board.dto.comment;

public record CreateCommentRequest(
        String content,
        Long parentCommentId
) {
}
