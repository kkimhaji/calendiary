package com.example.board.dto.role;

import com.example.board.dto.member.TeamMemberDTO;
import com.example.board.dto.member.TeamMemberInfoListDTO;
import com.example.board.permission.TeamPermission;

import java.util.List;
import java.util.Set;

public record RoleDetailsWithMemberListDTO(
        Long id,
        String roleName,
        String description,
        Set<TeamPermission> permissions,
        List<TeamMemberDTO> members
) {
}
