package com.example.board.dto.category;

import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.domain.category.TeamCategory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record CreateCategoryRequest(
   String name,
   String description,
   List<CategoryRolePermissionDTO> rolePermissions // roleId -> permissions mapping
) {
    public TeamCategory toEntity(Team team){
        return TeamCategory.createCategory(name,description, team);
    }

    public List<CategoryRolePermission> toCategoryRolePermissions(
            TeamCategory category,
            Map<Long, TeamRole> teamRoles) {
        return rolePermissions.stream()
                .map(rolePermDto -> rolePermDto.toEntity(
                        category,
                        teamRoles.get(rolePermDto.roleId())
                ))
                .collect(Collectors.toList());
    }

}
