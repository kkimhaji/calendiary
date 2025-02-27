package com.example.board.controller;

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
    public ResponseEntity<Boolean> checkPermission(@RequestParam PermissionType permission,
                                                   @RequestParam Long targetId){ //targetId: 팀 또는 카테고리 id
        return ResponseEntity.ok(permissionService.checkPermission(targetId, permission));
    }
}
