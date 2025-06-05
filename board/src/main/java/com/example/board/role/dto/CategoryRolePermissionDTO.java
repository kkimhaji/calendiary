package com.example.board.role.dto;

import com.example.board.permission.CategoryPermission;

import java.util.Set;

public record CategoryRolePermissionDTO(
        Long roleId,
        String roleName,
        Set<CategoryPermission> permissions
) {
}
