package com.example.board.service;

import com.example.board.permission.CategoryPermission;
import com.example.board.permission.PermissionType;
import com.example.board.permission.TeamPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService {
    //permission check 관련 메서드 옮길 것
    private final PermissionEvaluator permissionEvaluator;

    public boolean checkPermission(Long targetId, PermissionType permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String targetType = getTargetType(permission);
        return permissionEvaluator.hasPermission(auth, targetId, targetType, permission);
    }

    private String getTargetType(PermissionType permission) {
        if (permission instanceof TeamPermission) return "Team";
        if (permission instanceof CategoryPermission) return "TeamCategory";
        throw new IllegalArgumentException("Unsupported permission type");
    }
}
