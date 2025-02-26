package com.example.board.dto.role;

import com.example.board.domain.role.TeamRole;
import com.example.board.permission.PermissionUtils;
import com.example.board.permission.TeamPermission;

import java.util.Set;

public record TeamRoleDetailDto(
        Long id,
        String name,
        String permissionBits,
        long memberCount
) {}
