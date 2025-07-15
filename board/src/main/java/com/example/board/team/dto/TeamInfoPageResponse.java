package com.example.board.team.dto;

import com.example.board.team.enums.UserTeamStatus;
import com.example.board.teamMember.dto.TeamMemberInfo;
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
        TeamMemberInfo teamMemberInfo,
        UserTeamStatus userStatus
) {
    public static TeamInfoPageResponse fromTeamMember(TeamInfoDTO teamInfo, TeamMemberInfo memberInfo) {
        return new TeamInfoPageResponse(
                teamInfo.id(),
                teamInfo.name(),
                teamInfo.description(),
                teamInfo.created_by(),
                teamInfo.createdAt(),
                teamInfo.memberCount(),
                memberInfo,
                UserTeamStatus.TEAM_MEMBER
        );
    }

    public static TeamInfoPageResponse fromInvite(TeamInfoDTO teamInfo) {
        return new TeamInfoPageResponse(
                teamInfo.id(),
                teamInfo.name(),
                teamInfo.description(),
                teamInfo.created_by(),
                teamInfo.createdAt(),
                teamInfo.memberCount(),
                null,
                UserTeamStatus.VALID_INVITE
        );
    }
}
