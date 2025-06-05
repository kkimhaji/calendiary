package com.example.board.category.dto;

import com.example.board.role.CategoryRolePermission;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.utils.PermissionUtils;

import java.util.Set;

public record CategoryRolePermissionResponse(
        Long roleId,
        String roleName,
        Set<CategoryPermission> permissions //or Set<String>으로 권한 이름 전달
) {
    public static CategoryRolePermissionResponse from(CategoryRolePermission permission){
        return new CategoryRolePermissionResponse(
                permission.getRole().getId(),
                permission.getRole().getRoleName(),
                PermissionUtils.getPermissionsFromBits(permission.getPermissions(), CategoryPermission.class)
        );
    }
}
