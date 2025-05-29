package com.example.board.dto.role;

import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.permission.TeamPermission;
import lombok.Data;

import java.util.Objects;
import java.util.Set;

public record CreateRoleRequest(
        String roleName,
        Set<TeamPermission> permissions,
        String description) {

    public CreateRoleRequest{
        Objects.requireNonNull(roleName, "Role name must not be null");
        Objects.requireNonNull(permissions, "Permissions must not be null");
    }

    public TeamRole toEntity(Team team){
        return TeamRole.create(roleName, description, permissions, team);
    }
}