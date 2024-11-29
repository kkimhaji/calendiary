package com.example.board.dto.role;

import java.util.List;

public record AddMembersToRoleRequest(
        Long roleId,
        List<Long> members
) {
}
