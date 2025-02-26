package com.example.board.dto.role;

import com.example.board.permission.PermissionUtils;
import com.example.board.permission.TeamPermission;

import java.util.Set;

public record TeamRoleDetailResponse(
        Long id,
        String name,
        Set<TeamPermission> permissions,
        long memberCount
) {
//    public static TeamRoleDetailResponse of(TeamRoleDetailDto dto){
//        Set<TeamPermission> permissions = PermissionUtils.getPermissionsFromBits(dto.permissionBits(), TeamPermission.class);
//        return new TeamRoleDetailResponse(dto.id(), dto.name(), permissions, dto.memberCount());
//    }

}
