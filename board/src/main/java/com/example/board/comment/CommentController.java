package com.example.board.comment;

import com.example.board.auth.UserPrincipal;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.comment.dto.CreateCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category/{categoryId}/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasPermission(#categoryId, 'TeamCategory', T(com.example.board.permission.CategoryPermission).CREATE_COMMENT)")
    public ResponseEntity<CommentResponse> createComment(@PathVariable("postId") Long postId, @PathVariable("categoryId") @P("categoryId") Long categoryId, @RequestBody CreateCommentRequest request, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(commentService.createComment(user.getMember(), categoryId, postId, request));
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable("categoryId") Long categoryId, @PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,@AuthenticationPrincipal UserPrincipal user){
        commentService.deleteComment(commentId, user.getMember());
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsInPost(@PathVariable("categoryId") Long categoryId, @PathVariable("postId") Long postId){
        return ResponseEntity.ok(commentService.getCommentsInPost(postId));
    }
}
