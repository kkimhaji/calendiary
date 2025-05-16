package com.example.board.dto.comment;

import com.example.board.domain.post.Comment;
import com.example.board.domain.teamMember.TeamMember;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record CommentResponse(
        Long id,
        String content,
        Long authorId,
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

    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        String displayName = Optional.ofNullable(comment.getTeamMember())
                .map(TeamMember::getTeamNickname)
                .orElse("Unknown");

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getMemberId(),
                displayName,
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
