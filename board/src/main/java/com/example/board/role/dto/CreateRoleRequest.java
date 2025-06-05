package com.example.board.role.dto;

import com.example.board.permission.TeamPermission;

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
}