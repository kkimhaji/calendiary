package com.example.board.role.dto;

import java.util.List;

public record AddMembersToRoleResponse(
        String roleName,
        List<String> membersName
) {
}
