package com.example.board.dto.role;

import com.example.board.permission.TeamPermission;

import java.util.Set;

public record RoleUpdateRequest(
        String roleName,
        String description,
        Set<TeamPermission> permissions
) {
}
