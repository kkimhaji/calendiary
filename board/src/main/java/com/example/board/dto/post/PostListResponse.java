package com.example.board.dto.post;

import com.example.board.domain.post.Post;

import java.time.LocalDateTime;

public record PostListResponse(
        Long id,
        String title,
        String authorName,
        String categoryName,
        Long categoryId,
        int viewCount,
        LocalDateTime createdDate,
        long commentCount
) {
    public static PostListResponse from(Post post){
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
                post.getCategory().getName(),
                post.getCategory().getId(),
                post.getViewCount(),
                post.getCreatedDate(),
                post.getComments().size()
        );
    }
}
