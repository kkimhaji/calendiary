package com.example.board.dto.role;

public record TeamRoleDetailDto(
        Long id,
        String name,
        String permissionBits,
        long memberCount
) {}
