package com.example.board.dto.post;

import org.springframework.data.domain.Page;

public record CategoryRecentPostsResponse(
        String categoryName,
        Page<PostListResponse> posts
) {
}
