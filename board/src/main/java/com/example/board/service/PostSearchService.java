package com.example.board.service;

import com.example.board.config.AsyncConfig;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.dto.post.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PostSearchService {
    private final PostRepository postRepository;
    private final AsyncConfig asyncConfig;

    public Page<PostResponse> searchPosts(Long teamId, String keyword, Pageable pageable) {
        return postRepository.searchByTeamAndKeyword(teamId, keyword, pageable)
                .map(PostResponse::from); // 메서드 참조 사용
    }
}
