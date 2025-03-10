package com.example.board.dto.member;

import com.example.board.domain.teamMember.TeamMember;

public record TeamMemberOfRoleDTO(
        Long id,
        String email,
        String teamNickname,
        String roleName
) {
    public static TeamMemberOfRoleDTO from(TeamMember teamMember){
        return new TeamMemberOfRoleDTO(
            teamMember.getId(),
            teamMember.getMember().getEmail(),
            teamMember.getTeamNickname(),
            teamMember.getRole().getRoleName()
        );
    }
}
