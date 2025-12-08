package com.example.board.comment.dto;

import com.example.board.comment.Comment;
import com.example.board.teamMember.TeamMember;

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

    /**
     * 엔티티에서 관리되는 replies를 그대로 변환
     */
    public static CommentResponse from(Comment comment) {
        // 엔티티에서 이미 정리된 replies를 단순 변환
        List<CommentResponse> replyResponses = comment.getReplies().stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        String displayName = Optional.ofNullable(comment.getTeamMember())
                .map(TeamMember::getTeamNickname)
                .orElse("Unknown");

        Long authorId = Optional.ofNullable(comment.getTeamMember())
                .map(TeamMember::getId)
                .orElse(null);

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                authorId,
                displayName,
                comment.getCreatedDate(),
                comment.isDeleted(),
                replyResponses
        );
    }
}
