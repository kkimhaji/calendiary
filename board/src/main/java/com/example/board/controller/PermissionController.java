package com.example.board.controller;

import com.example.board.dto.role.EditAndDeletePermissionResponse;
import com.example.board.permission.PermissionType;
import com.example.board.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping("/permission-check")
    public ResponseEntity<Boolean> checkPermission(@RequestParam("permission") PermissionType permission,
                                                   @RequestParam("targetId") Long targetId){ //targetId: 팀 또는 카테고리 id
        return ResponseEntity.ok(permissionService.checkPermission(targetId, permission));
    }

    @GetMapping("/edit-delete-check/post")
    public ResponseEntity<EditAndDeletePermissionResponse> checkPostPermission(@RequestParam("postId") Long postId){
        return ResponseEntity.ok(permissionService.checkEditAndDeletePostPermission(postId));
    }

    @GetMapping("/edit-delete-check/comment")
    public ResponseEntity<EditAndDeletePermissionResponse> checkCommentPermission(@RequestParam("commentId") Long commentId){
        return ResponseEntity.ok(permissionService.checkEditAndDeleteCommentPermission(commentId));
    }
}
