package com.example.board.controller;

import com.example.board.domain.member.Member;
import com.example.board.dto.comment.CommentResponse;
import com.example.board.dto.comment.CreateCommentRequest;
import com.example.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long teamId, @PathVariable Long postId, @RequestBody CreateCommentRequest request, @AuthenticationPrincipal Member member){
        return ResponseEntity.ok(commentService.createComment(member, postId, teamId, request));
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long teamId, @PathVariable Long postId, @PathVariable Long commentId,@AuthenticationPrincipal Member member){
        commentService.deleteComment(teamId, commentId, member);
    }
}
