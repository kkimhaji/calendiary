package com.example.board.post.dto;

import org.springframework.data.domain.Page;

public record TeamRecentPostsResponse(
        String teamName,
        Page<PostListResponse> posts
) {
}
