package com.example.board.dto.team;

import com.example.board.domain.team.enums.UserTeamStatus;
import com.example.board.dto.teamMember.TeamNicknameAndRoleName;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

public record TeamInfoPageResponse(
        Long id,
        String name,
        String description,
        String created_by,
        LocalDateTime createdAt,
        long memberCount,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String teamNickname,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String roleName,
        UserTeamStatus userStatus
) {
    public static TeamInfoPageResponse fromTeamMember(TeamInfoDTO teamInfo, TeamNicknameAndRoleName memberInfo) {
        return new TeamInfoPageResponse(
                teamInfo.id(), teamInfo.name(), teamInfo.description(),
                teamInfo.created_by(), teamInfo.createdAt(),
                teamInfo.memberCount(),
                memberInfo.teamNickname(), memberInfo.roleName(),
                UserTeamStatus.TEAM_MEMBER
        );
    }

    public static TeamInfoPageResponse fromInvite(TeamInfoDTO teamInfo) {
        return new TeamInfoPageResponse(
                teamInfo.id(), teamInfo.name(), teamInfo.description(),
                teamInfo.created_by(), teamInfo.createdAt(),
                teamInfo.memberCount(),
                null, null,
                UserTeamStatus.VALID_INVITE
        );
    }

    public static TeamInfoPageResponse noAccess(TeamInfoDTO teamInfo) {
        return new TeamInfoPageResponse(
                teamInfo.id(), teamInfo.name(), teamInfo.description(),
                teamInfo.created_by(), teamInfo.createdAt(),
                teamInfo.memberCount(),
                null, null,
                UserTeamStatus.NO_ACCESS
        );
    }
}
