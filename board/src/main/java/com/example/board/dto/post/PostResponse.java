package com.example.board.dto.post;

import com.example.board.domain.post.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String authorName,
        Long teamId,
        Long categoryId,
        String categoryName,
        int viewCount,
        LocalDateTime createdDate,
        long commentCount
) {
    public static PostResponse from(Post post){
        String nickname = "Unknown";
        if (post.getTeamMember() != null) {
            nickname = post.getTeamMember().getTeamNickname();
            if (nickname.length() > 7) {
                nickname = nickname.substring(0, 5) + "...";
            }
        }
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                nickname,
                post.getTeam().getId(),
                post.getCategory() != null ? post.getCategory().getId() : null,
                post.getCategory() != null ? post.getCategory().getName() : "No Category",
                post.getViewCount(),
                post.getCreatedDate(),
                post.getComments().size()
        );
    }
}
