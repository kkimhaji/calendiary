package com.example.board.dto.role;

import java.util.List;

public record AddMembersToRoleResponse(
        String roleName,
        List<String> membersName
) {
}
