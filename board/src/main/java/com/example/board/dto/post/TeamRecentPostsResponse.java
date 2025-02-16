package com.example.board.dto.post;

import org.springframework.data.domain.Page;

public record TeamRecentPostsResponse(
        String teamName,
        Page<PostListResponse> posts
) {
}
