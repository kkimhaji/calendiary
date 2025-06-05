package com.example.board.teamMember.dto;

import java.time.LocalDateTime;

public record TeamMemberInfo(
        String teamNickname,
        String roleName,
        LocalDateTime joinedAt
) {
}
