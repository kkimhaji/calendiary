package com.example.board.dto.comment;

import com.example.board.domain.post.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
package com.example.board.dto.comment;

import com.example.board.domain.post.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CommentResponse(
        Long id,
        String content,
        String authorName,
        LocalDateTime createdDate,
        boolean isDeleted,
        List<CommentResponse> replies
) {

    // 기존 from 메서드 (단일 댓글 변환)
    public static CommentResponse from(Comment comment) {
        return from(comment, comment.getReplies().stream()
                .map(reply -> from(reply, reply.getReplies().stream()
                        .map(CommentResponse::from)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList()));
    }

    // 새로운 from 메서드 (계층 구조 생성을 위한 오버로딩)
    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor() != null ? comment.getAuthor().getNickname() : "Unknown", // 작성자 정보 처리
                comment.getCreatedDate(),
                comment.isDeleted(),
                replies
        );
    }

    public static CommentResponse convertToResponse(Comment comment, List<Comment> allComments) {
        List<CommentResponse> replies = allComments.stream()
                .filter(c -> c.getParent() != null && c.getParent().getId().equals(comment.getId()))
                .map(c -> convertToResponse(c, allComments))
                .collect(Collectors.toList());

        return CommentResponse.from(comment, replies);
    }
}
