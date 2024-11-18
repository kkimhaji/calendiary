package com.example.board.dto.category;

import java.util.List;
import java.util.Set;

public record CategoryRolePermissionRequest(
        Long roleId,
        Set<String> permissions
) {
}
