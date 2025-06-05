package com.example.board.comment.dto;

import jakarta.annotation.Nullable;

public record CreateCommentRequest(
        String content,
        @Nullable Long parentCommentId //대댓글인 경우
//        int depth
) {
}
