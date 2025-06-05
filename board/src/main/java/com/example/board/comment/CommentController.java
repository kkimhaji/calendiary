package com.example.board.comment;

import com.example.board.auth.UserPrincipal;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.comment.dto.CreateCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@PathVariable("postId") Long postId, @RequestBody CreateCommentRequest request, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(commentService.createComment(user.getMember(), postId, request));
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,@AuthenticationPrincipal UserPrincipal user){
        commentService.deleteComment(commentId, user.getMember());
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsInPost(@PathVariable("postId") Long postId){
        return ResponseEntity.ok(commentService.getCommentsInPost(postId));
    }
}
