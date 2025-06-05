package com.example.board.post.dto;

import org.springframework.data.domain.Page;

public record CategoryRecentPostsResponse(
        String categoryName,
        Page<PostListResponse> posts
) {
}
