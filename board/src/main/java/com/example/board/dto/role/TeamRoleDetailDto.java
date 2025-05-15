package com.example.board.dto.role;

public record TeamRoleDetailDto(
        Long id,
        String name,
        byte[] permissionBits,
        long memberCount
) {}
