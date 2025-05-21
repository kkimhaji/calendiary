package com.example.board.dto.comment;

import com.example.board.domain.comment.Comment;

import java.time.LocalDateTime;

public record MemberCommentResponse(
        Long id,
        String content,
        Long authorId,
        String authorName,
        LocalDateTime createdDate,
        boolean isDeleted,
        Long postId,
        String postTitle,
        Long categoryId,
        Long teamId
) {
    public static MemberCommentResponse from(Comment comment) {
        String nickname = "Unknown";
        if (comment.getTeamMember() != null) {
            nickname = comment.getTeamMember().getTeamNickname();
            if (nickname.length() > 7) {
                nickname = nickname.substring(0, 5) + "...";
            }
        }
        return new MemberCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getMemberId(),
                nickname,
                comment.getCreatedDate(),
                comment.isDeleted(),
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                comment.getPost().getCategory().getId(),
                comment.getPost().getCategory().getTeam().getId()
        );
    }
}

