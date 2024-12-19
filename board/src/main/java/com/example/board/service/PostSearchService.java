package com.example.board.service;

import com.example.board.config.AsyncConfig;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
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

    public CompletableFuture<List<Post>> searchByTitle(String keyword){
        return CompletableFuture.supplyAsync(() ->
                postRepository.findByTitleContaining(keyword), asyncConfig.getAsyncExecutor());
    }

    public CompletableFuture<List<Post>> searchByContent(String keyword){
        return CompletableFuture.supplyAsync(() ->
                postRepository.findByContentContaining(keyword), asyncConfig.getAsyncExecutor());
    }

    public List<Post> searchPosts(String keyword){
        CompletableFuture<List<Post>> titleSearch = searchByTitle(keyword);
        CompletableFuture<List<Post>> contentSearch = searchByContent(keyword);

        List<Post> results = CompletableFuture.allOf(titleSearch, contentSearch)
                .thenApply(v -> {
                    Set<Post> combinedResults = new HashSet<>();
                    combinedResults.addAll(titleSearch.join());
                    combinedResults.addAll(contentSearch.join());

                    return new ArrayList<>(combinedResults);
                })
                .join();

        return results;
    }
}
