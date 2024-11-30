package com.example.board.dto.category;

import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.TeamCategory;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

public record UpdateCategoryRequest(
        String name,
        String description,
        Optional<List<CategoryRolePermissionDTO>> rolePermissions //권한 수정 선택적
) {
    public void updateEntity(TeamCategory category){
        if (name != null)
            category.updateName(name);
        if (description != null)
            category.updateDescription(description);
    }

    public List<CategoryRolePermission> toCategoryRolePermissions(
            TeamCategory category, Map<Long, TeamRole> teamRoles
    ){

        return rolePermissions.map(permissions -> permissions.stream()
                .map(rolePermissionDto -> rolePermissionDto.toEntity(category, teamRoles.get(rolePermissionDto.roleId())))
                .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
