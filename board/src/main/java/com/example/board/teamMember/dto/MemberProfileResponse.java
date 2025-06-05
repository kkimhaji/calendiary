package com.example.board.teamMember.dto;

import java.time.LocalDateTime;

public record MemberProfileResponse(
        String email,
        String teamNickname,
        String roleName,
        LocalDateTime joinedAt
) {
}
