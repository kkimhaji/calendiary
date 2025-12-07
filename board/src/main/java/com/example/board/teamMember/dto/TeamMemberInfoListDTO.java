package com.example.board.teamMember.dto;

public record TeamMemberInfoListDTO(
        Long teamMemberId,
        String email,
        String teamNickname,
        String roleName,
        Long roleId
) {
}
