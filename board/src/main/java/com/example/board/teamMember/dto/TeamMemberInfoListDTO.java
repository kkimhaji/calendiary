package com.example.board.teamMember.dto;

public record TeamMemberInfoListDTO(
        String email,
        String teamNickname,
        String roleName,
        Long roleId
) {
}
