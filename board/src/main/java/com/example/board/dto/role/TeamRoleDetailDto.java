package com.example.board.dto.role;

import com.example.board.permission.PermissionUtils;
import com.example.board.permission.TeamPermission;

import java.util.Set;

public record TeamRoleDetailDto(
        Long id,
        String name,
        Set<TeamPermission> permissions,
        long memberCount
) {
    public static TeamRoleDetailDto of(Long id, String name, String permissionBits, long memberCount){
        Set<TeamPermission> permissions = PermissionUtils.getPermissionsFromBits(permissionBits, TeamPermission.class);
        return new TeamRoleDetailDto(id, name, permissions, memberCount);
    }
}
