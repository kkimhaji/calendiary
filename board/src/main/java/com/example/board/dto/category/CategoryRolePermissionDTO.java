package com.example.board.dto.category;

import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.TeamCategory;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.TeamPermission;

import java.util.Set;

public record CategoryRolePermissionDTO(
        Long roleId,
        Set<CategoryPermission> permissions
) {
    public CategoryRolePermission toEntity(TeamCategory category, TeamRole role) {
        return CategoryRolePermission.builder()
                .category(category)
                .role(role)
                .permissions(permissions)
                .build();
    }

}
