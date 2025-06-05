package com.example.board.role.dto;

import com.example.board.permission.TeamPermission;

import java.util.Set;

public record TeamRoleDetailResponse(
        Long id,
        String name,
        Set<TeamPermission> permissions,
        long memberCount
) {
}