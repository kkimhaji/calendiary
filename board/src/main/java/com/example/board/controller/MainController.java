package com.example.board.controller;

import com.example.board.auth.UserPrincipal;
import com.example.board.dto.post.PostResponse;
import com.example.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MainController {
    private final PostService postService;

    @GetMapping("/main")
    public ResponseEntity<Page<PostResponse>> getUserTeamPosts(
            @AuthenticationPrincipal UserPrincipal user, @PageableDefault(size=10)Pageable pageable
            ){
        return ResponseEntity.ok(postService.getLatestPostsByUserTeams(user, pageable));
    }
}