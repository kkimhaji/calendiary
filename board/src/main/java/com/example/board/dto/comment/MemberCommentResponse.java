package com.example.board.dto.comment;

import com.example.board.domain.post.Comment;

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
    // 팩토리 메서드로 변환 가능
    public static MemberCommentResponse from(Comment comment) {
        return new MemberCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getMemberId(),
                comment.getTeamMember().getTeamNickname(),
                comment.getCreatedDate(),
                comment.isDeleted(),
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                comment.getPost().getCategory().getId(),
                comment.getPost().getCategory().getTeam().getId()
        );
    }
}

