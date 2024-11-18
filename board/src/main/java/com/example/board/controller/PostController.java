package com.example.board.controller;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.dto.post.CreatePostRequest;
import com.example.board.dto.post.PostListResponse;
import com.example.board.dto.post.PostResponse;
import com.example.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/category/{categoryId}/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@PathVariable Long teamId, @PathVariable Long categoryId, @RequestBody CreatePostRequest request, @AuthenticationPrincipal Member member){
        Post post = postService.createPost(teamId, request, member);
        return ResponseEntity.ok(PostResponse.from(post));
    }

    @GetMapping
    public ResponseEntity<Page<PostListResponse>> getPosts(
            @PathVariable Long teamId, @PathVariable Long categoryId,
            @AuthenticationPrincipal Member member,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable
            ){
        Page<PostListResponse> posts = postService.getPostsByCategory(teamId, categoryId, member, pageable);
        return ResponseEntity.ok(posts);
    }

}
