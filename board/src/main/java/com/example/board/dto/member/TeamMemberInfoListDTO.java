package com.example.board.dto.member;

public record TeamMemberInfoListDTO(
        String email,
        String teamNickname,
        String roleName,
        Long roleId
) {
}
