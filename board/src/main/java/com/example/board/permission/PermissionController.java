package com.example.board.permission;

import com.example.board.role.dto.EditAndDeletePermissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping("/permission-check")
    public ResponseEntity<Boolean> checkPermission(@RequestParam("permission") PermissionType permission,
                                                   @RequestParam("targetId") Long targetId){ //targetId: 팀 또는 카테고리 id
        return ResponseEntity.ok(permissionService.checkPermission(targetId, permission));
    }

    @GetMapping("/permissions-check")
    public ResponseEntity<Map<String, Boolean>> checkMultiplePermissions(@RequestParam("permissions")List<String> permissions,
                                                                         @RequestParam("targetId") Long targetId){
        return ResponseEntity.ok(permissionService.checkMultiplePermission(targetId, permissions));
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
