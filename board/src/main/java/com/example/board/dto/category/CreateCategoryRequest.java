package com.example.board.dto.category;

import java.util.Map;
import java.util.Set;

public record CreateCategoryRequest(
   String name,
   String description,
   Map<Long, Set<String>> rolePermissions // roleId -> permissions mapping
) {
}
