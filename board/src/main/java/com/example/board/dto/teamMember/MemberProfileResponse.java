package com.example.board.dto.teamMember;

import java.time.LocalDateTime;

public record MemberProfileResponse(
        String email,
        String teamNickname,
        String roleName,
        LocalDateTime joinedAt
) {
}
