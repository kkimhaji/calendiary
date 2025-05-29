package com.example.board.dto.category;

import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.category.TeamCategory;

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
}
