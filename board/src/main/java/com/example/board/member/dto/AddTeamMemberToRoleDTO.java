package com.example.board.member.dto;

import com.example.board.teamMember.TeamMember;

public record AddTeamMemberToRoleDTO(
        Long id,          // TeamMember 엔티티의 ID
        Long memberId,    // Member 엔티티의 ID
        String email,
        String teamNickname,
        String roleName   // 현재 역할 이름
) {
    public AddTeamMemberToRoleDTO(TeamMember teamMember) {
        this(
                teamMember.getId(),
                teamMember.getMember().getMemberId(),
                teamMember.getMember().getEmail(),
                teamMember.getTeamNickname(),
                teamMember.getRole().getRoleName()
        );
    }
}
