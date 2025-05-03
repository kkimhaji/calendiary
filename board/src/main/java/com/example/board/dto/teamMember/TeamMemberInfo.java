package com.example.board.dto.teamMember;

import java.time.LocalDateTime;

public record TeamMemberInfo(
        String teamNickname,
        String roleName,
        LocalDateTime joinedAt
) {
}
