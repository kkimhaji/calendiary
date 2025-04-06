package com.example.board.dto.team;

import com.example.board.dto.member.TeamNicknameAndRoleName;

import java.time.LocalDateTime;

public record TeamInfoPageResponse(
        Long id,
        String name,
        String description,
        String created_by,
        LocalDateTime createdAt,
        long memberCount,
        String teamNickname,
        String roleName
) {
    public static TeamInfoPageResponse from(TeamInfoDTO teamInfo, TeamNicknameAndRoleName memberInfo){
        return new TeamInfoPageResponse(
                teamInfo.id(), teamInfo.name(), teamInfo.description(),
                teamInfo.created_by(), teamInfo.createdAt(),
                teamInfo.memberCount(),
                memberInfo.teamNickname(), memberInfo.roleName()
        );
    }
}
