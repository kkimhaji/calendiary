package com.example.board.role.dto;

import java.util.List;

public record AddMembersToRoleRequest(
        Long roleId,
        List<Long> members
) {
}
