package com.example.board.dto.role;

import com.example.board.domain.team.TeamRole;
import com.example.board.permission.TeamPermission;

import java.util.Set;

public record TeamRoleResponse(Long id, String name, Set<TeamPermission> permissions, Long teamId) {
    public static TeamRoleResponse from(TeamRole role) {
        return new TeamRoleResponse(
                role.getId(),
                role.getRoleName(),
                role.getPermissionSet(),
                role.getTeam().getTeamId()
        );
    }
}
