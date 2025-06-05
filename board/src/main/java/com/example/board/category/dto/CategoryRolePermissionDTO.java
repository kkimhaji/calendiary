package com.example.board.category.dto;

import com.example.board.permission.CategoryPermission;

import java.util.Set;

public record CategoryRolePermissionDTO(
        Long roleId,
        Set<CategoryPermission> permissions
) {
}