package com.example.board.post.dto;

import com.example.board.post.Post;

import java.time.LocalDateTime;

public record PostListResponse(
        Long id,
        String title,
        String authorName,
        Long teamId,
        String categoryName,
        Long categoryId,
        int viewCount,
        LocalDateTime createdDate,
        long commentCount
) {
    public static PostListResponse from(Post post) {
        String nickname = "Unknown";
        if (post.getTeamMember() != null) {
            nickname = post.getTeamMember().getTeamNickname();
            if (nickname.length() > 7) {
                nickname = nickname.substring(0, 5) + "...";
            }
        }
        return new PostListResponse(
                post.getId(),
                post.getTitle(),
                nickname,
                post.getTeam().getId(),
                post.getCategory().getName(),
                post.getCategory().getId(),
                post.getViewCount(),
                post.getCreatedDate(),
                post.getComments().size()
        );
    }
}
