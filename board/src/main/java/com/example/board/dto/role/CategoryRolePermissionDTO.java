package com.example.board.dto.role;

import com.example.board.permission.CategoryPermission;

import java.util.Set;

public record CategoryRolePermissionDTO(
        Long roleId,
        String roleName,
        Set<CategoryPermission> permissions
) {
}
