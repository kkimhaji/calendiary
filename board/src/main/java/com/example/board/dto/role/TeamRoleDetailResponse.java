package com.example.board.dto.role;

import com.example.board.permission.TeamPermission;

import java.util.Set;

public record TeamRoleDetailResponse(
        Long id,
        String name,
        Set<TeamPermission> permissions,
        long memberCount
) {
}