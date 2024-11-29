package com.example.board.dto.category;

import com.example.board.domain.team.TeamCategory;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

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
