package com.example.board.dto.category;

import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.category.TeamCategory;
import com.example.board.permission.CategoryPermission;

import java.util.Set;

public record CategoryRolePermissionDTO(
        Long roleId,
        Set<CategoryPermission> permissions
) {
    public CategoryRolePermission toEntity(TeamCategory category, TeamRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role not found for ID: " + roleId);
        }

        return CategoryRolePermission.builder()
                .category(category)
                .role(role)
                .permissions(permissions)
                .build();
    }

}
