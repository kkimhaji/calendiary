package com.example.board.dto.teamMember;

public record TeamMemberInfoListDTO(
        String email,
        String teamNickname,
        String roleName,
        Long roleId
) {
}
