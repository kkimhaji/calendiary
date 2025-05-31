package com.example.board.dto.category;

import java.util.List;

public record CreateCategoryRequest(
        String name,
        String description,
        List<CategoryRolePermissionDTO> rolePermissions // roleId -> permissions mapping
) {
    public void validate(){
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
    }
}
