package com.example.board.dto.role;

import com.example.board.permission.TeamPermission;
import com.example.board.domain.role.TeamRole;
import java.util.Set;

public record TeamRoleResponse(Long id, String name, String description, Set<TeamPermission> permissions, Long teamId) {
    public static TeamRoleResponse from(TeamRole role) {
        return new TeamRoleResponse(
                role.getId(),
                role.getRoleName(),
                role.getDescription(),
                role.getPermissionSet(),
                role.getTeam().getId()
        );
    }
}
