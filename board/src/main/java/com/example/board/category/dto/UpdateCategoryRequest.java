package com.example.board.category.dto;

import java.util.*;

public record UpdateCategoryRequest(
        String name,
        String description,
        Optional<List<CategoryRolePermissionDTO>> rolePermissions //권한 수정 선택적
) {
}