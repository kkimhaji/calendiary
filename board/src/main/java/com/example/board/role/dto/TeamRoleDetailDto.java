package com.example.board.role.dto;

public record TeamRoleDetailDto(
        Long id,
        String name,
        byte[] permissionBits,
        long memberCount
) {}
